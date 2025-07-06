package net.shuyanmc.mpem;


import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.shuyanmc.mpem.config.CoolConfig;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ModEventProcessor {
    private static final Type SUBSCRIBE_EVENT = Type.getType(SubscribeEvent.class);
    private static final String CLIENT_ONLY_WARNING = "Skipping client-side class loading on server: ";

    private static final Set<String> CLASS_BLACKLIST_CACHE = ConcurrentHashMap.newKeySet();
    private static final Set<String> MOD_BLACKLIST_CACHE = ConcurrentHashMap.newKeySet();
    private static final Set<String> PROCESSED_CLASSES = ConcurrentHashMap.newKeySet();

    public static void initialize() {
        loadBlacklists();
    }

    public static void processModEvents() {
        if (!FMLEnvironment.dist.isClient()) {
            AsyncEventSystem.LOGGER.info("Server environment detected - skipping client event processing");
            return;
        }

        List<ModFileScanData> allScanData = ModList.get().getAllScanData();
        Set<String> eventMethods = new HashSet<>();

        for (ModFileScanData scanData : allScanData) {
            // 兼容性获取modId的方式
            String modId = getModIdFromScanData(scanData);
            if (modId == null || MOD_BLACKLIST_CACHE.contains(modId)) {
                AsyncEventSystem.LOGGER.debug("Skipping blacklisted or invalid mod: {}", modId);
                continue;
            }

            for (ModFileScanData.AnnotationData ad : scanData.getAnnotations()) {
                try {
                    if (SUBSCRIBE_EVENT.equals(ad.annotationType())) {
                        processAnnotationData(ad, eventMethods);
                    }
                } catch (Exception | Error e) {
                    try {
                        FileUtils.writeStringToFile(MpemMod.MPEM_EVENTS_LOG,"\n"+ e,true);
                        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                            FileUtils.writeStringToFile(MpemMod.MPEM_EVENTS_LOG,"\n"+stackTraceElement.toString(),true);
                        }
                        for (Throwable stackTraceElement : e.getSuppressed()) {
                            FileUtils.writeStringToFile(MpemMod.MPEM_EVENTS_LOG,"\n"+stackTraceElement.toString(),true);
                        }
                    } catch (IOException ex) {
                        e.printStackTrace();
                    }
                }
            }
        }

        AsyncEventSystem.LOGGER.info("Found {} event methods in mods", eventMethods.size());
        processEventMethods(eventMethods);
    }

    private static String getModIdFromScanData(ModFileScanData scanData) {
        try {
            // 尝试多种方式获取modId
            if (!scanData.getIModInfoData().isEmpty()) {
                return scanData.getIModInfoData().get(0).getMods().get(0).getModId();
            }
            /*
            if (scanData.getTargets() != null && !scanData.getTargets().isEmpty()) {
                return scanData.getTargets().keySet().iterator().next();
            }*/
            return null;
        } catch (Exception e) {
            AsyncEventSystem.LOGGER.warn("Failed to get modId from scan data", e);
            return null;
        }
    }

    private static void processAnnotationData(ModFileScanData.AnnotationData ad, Set<String> eventMethods) {
        String className;

        try {
            className = ad.clazz().getClassName();
            Class.forName(className);
            System.out.println("TESTED A CLASS: "+className);
        } catch (ClassNotFoundException | NoClassDefFoundError | ClassCastException e) {
            AsyncEventSystem.LOGGER.debug("Skipped a class.");
            return;
        }

        if (PROCESSED_CLASSES.contains(className)) {
            return;
        }

        if (isBlacklisted(className)) {
            AsyncEventSystem.LOGGER.debug("Skipping blacklisted class: {}", className);
            PROCESSED_CLASSES.add(className);
            return;
        }

        if (isClientOnlyClass(className)) {
            AsyncEventSystem.LOGGER.debug(CLIENT_ONLY_WARNING + className);
            PROCESSED_CLASSES.add(className);
            return;
        }

        if (CoolConfig.isStrictClassCheckingEnabled()) {
            try {
                Class.forName(className, false, ModEventProcessor.class.getClassLoader());
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                AsyncEventSystem.LOGGER.debug("Class loading failed: {}", className);
                PROCESSED_CLASSES.add(className);
                return;
            }
        }

        eventMethods.add(className + "#" + ad.memberName());
        PROCESSED_CLASSES.add(className);
    }

    private static void processEventMethods(Set<String> eventMethods) {
        for (String methodInfo : eventMethods) {
            try {
                String[] parts = methodInfo.split("#");
                String className = parts[0];
                String methodName = parts[1];

                try {
                    Class.forName(className);
                    System.out.println("TESTED A CLASS: "+className);
                } catch (ClassNotFoundException | NoClassDefFoundError | ClassCastException e) {
                    AsyncEventSystem.LOGGER.debug("Skipped a class: {}",className);
                    continue;
                }

                if (isClientOnlyClass(className)) {
                    AsyncEventSystem.LOGGER.warn(CLIENT_ONLY_WARNING + className);
                    continue;
                }

                Class<?> clazz = Class.forName(className);
                if(clazz.equals(EnchantmentLevelSetEvent.class) || clazz.equals(GetEnchantmentLevelEvent.class)){
                    AsyncEventSystem.LOGGER.info("Skipped a event what will case bugs: {}",className);
                }
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().equals(methodName)) {
                        Class<?>[] params = method.getParameterTypes();
                        if (params.length == 1 && Event.class.isAssignableFrom(params[0])) {
                            @SuppressWarnings("unchecked")
                            Class<? extends Event> eventType = (Class<? extends Event>) params[0];

                            if (!isBlacklisted(eventType.getName())) {
                                AsyncEventSystem.registerAsyncEvent(eventType);
                                AsyncEventSystem.LOGGER.debug("Registered async event: {}", eventType.getName());
                            }
                        }
                    }
                }
            } catch (NoClassDefFoundError | ClassNotFoundException e) {
                if (e.getMessage().contains("client/renderer")) {
                    AsyncEventSystem.LOGGER.warn(CLIENT_ONLY_WARNING + e.getMessage());
                } else {
                    AsyncEventSystem.LOGGER.error("Class loading failed: {}", methodInfo, e);
                }
            } catch (Exception e) {
                AsyncEventSystem.LOGGER.error("Failed to process event method {}", methodInfo, e);
            }
        }
    }

    public static void loadBlacklists() {
        CLASS_BLACKLIST_CACHE.clear();
        MOD_BLACKLIST_CACHE.clear();

        CLASS_BLACKLIST_CACHE.addAll(CoolConfig.getAsyncEventClassBlacklist());
        MOD_BLACKLIST_CACHE.addAll(CoolConfig.getAsyncEventModBlacklist());

        AsyncEventSystem.LOGGER.info("Loaded {} class blacklist entries and {} mod blacklist entries",
                CLASS_BLACKLIST_CACHE.size(), MOD_BLACKLIST_CACHE.size());
    }

    private static boolean isBlacklisted(String className) {
        if (CLASS_BLACKLIST_CACHE.contains(className)) {
            return true;
        }

        for (String pattern : CLASS_BLACKLIST_CACHE) {
            if (pattern.endsWith(".*") && className.startsWith(pattern.substring(0, pattern.length() - 1))) {
                return true;
            }
        }

        return false;
    }

    private static boolean isClientOnlyClass(String className) {
        return className.startsWith("net.minecraft.client.") ||
                className.contains(".client.") ||
                className.endsWith("ClientEvents");
    }
}