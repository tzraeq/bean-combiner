package org.tzraeq.idea.plugin.beancombiner.util;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import icons.BeanCombinerIcons;
import org.jetbrains.annotations.Nullable;

/**
 * 从2020.3版本开始，notification group开始可以用extension的方式，从2021.3版本开始，notification group必须用extension的方式
 */
public class NotificationUtil {
    /*private static final NotificationGroup NOTIFICATION_GROUP =
            new NotificationGroup("Bean Combiner", NotificationDisplayType.BALLOON, true);*/

    public static void notifyError(@Nullable Project project, String content) {
        Notifications.Bus.notify(new Notification(content, BeanCombinerIcons.ConfigFile, NotificationType.ERROR), project);
        /*NOTIFICATION_GROUP.createNotification(content, NotificationType.ERROR)
                .setIcon(BeanCombinerIcons.ConfigFile)
                .notify(project);*/
    }
}
