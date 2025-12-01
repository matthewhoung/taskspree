package com.hongsolo.taskspree.modules.marketplace.domain.role.enums;

import java.util.Set;

public enum RoleType {
    OWNER(
            "Owner",
            "The owner of this marketplace",
            Set.of(
                    Permission.MANAGE_MARKETPLACE,
                    Permission.MANAGE_MEMBERS,
                    Permission.PUBLISH_TASK,
                    Permission.EDIT_ANY_TASK,
                    Permission.CLOSE_TASK,
                    Permission.VIEW_ANALYTICS
            )
    ),
    MANAGER(
            "Manager",
            "Manages tasks and members",
            Set.of(
                    Permission.MANAGE_MEMBERS,
                    Permission.PUBLISH_TASK,
                    Permission.EDIT_ANY_TASK,
                    Permission.CLOSE_TASK,
                    Permission.VIEW_ANALYTICS
            )
    ),
    MEMBER(
            "Member",
            "Can browse and shop tasks",
            Set.of(
                    Permission.VIEW_ANALYTICS
            )
    );

    private final String defaultDisplayName;
    private final String defaultDescription;
    private final Set<Permission> permissions;

    RoleType(String defaultDisplayName, String defaultDescription, Set<Permission> permissions) {
        this.defaultDisplayName = defaultDisplayName;
        this.defaultDescription = defaultDescription;
        this.permissions = permissions;
    }

    public String getDefaultDisplayName() {
        return defaultDisplayName;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}