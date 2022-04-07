package org.tzraeq.idea.plugin.beancombiner.util;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import icons.BeanCombinerIcons;
import org.jetbrains.annotations.Nullable;

public class NotificationUtil {
    private static final NotificationGroup NOTIFICATION_GROUP =
            new NotificationGroup("Bean Combiner", NotificationDisplayType.BALLOON, true);

    public static void notifyError(@Nullable Project project, String content) {
        NOTIFICATION_GROUP.createNotification(content, NotificationType.ERROR)
                .setIcon(BeanCombinerIcons.ConfigFile)
                .notify(project);
    }
}
