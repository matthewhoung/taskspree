package com.hongsolo.taskspree.modules.marketplace.domain.marketplace;

import com.hongsolo.taskspree.common.domain.Error;

public final class MarketplaceErrors {

    private MarketplaceErrors() {
        // Prevent instantiation
    }

    // === Marketplace Errors ===

    public static final Error MARKETPLACE_NOT_FOUND = new Error(
            "Marketplace.NotFound",
            "Marketplace not found",
            Error.ErrorType.NOT_FOUND
    );

    public static final Error MARKETPLACE_ARCHIVED = new Error(
            "Marketplace.Archived",
            "This marketplace has been archived",
            Error.ErrorType.VALIDATION
    );

    public static final Error SLUG_ALREADY_EXISTS = new Error(
            "Marketplace.SlugAlreadyExists",
            "A marketplace with this slug already exists",
            Error.ErrorType.CONFLICT
    );

    public static final Error CANNOT_ARCHIVE_ONLY_MARKETPLACE = new Error(
            "Marketplace.CannotArchiveOnlyMarketplace",
            "Cannot archive your only marketplace",
            Error.ErrorType.VALIDATION
    );

    // === Permission Errors ===

    public static final Error ACCESS_DENIED = new Error(
            "Marketplace.AccessDenied",
            "You do not have permission to perform this action",
            Error.ErrorType.VALIDATION
    );

    public static final Error NOT_A_MEMBER = new Error(
            "Marketplace.NotAMember",
            "You are not a member of this marketplace",
            Error.ErrorType.VALIDATION
    );

    public static final Error INSUFFICIENT_PERMISSIONS = new Error(
            "Marketplace.InsufficientPermissions",
            "You do not have the required permissions",
            Error.ErrorType.VALIDATION
    );

    // === Member Errors ===

    public static final Error MEMBER_NOT_FOUND = new Error(
            "Marketplace.MemberNotFound",
            "Member not found",
            Error.ErrorType.NOT_FOUND
    );

    public static final Error ALREADY_A_MEMBER = new Error(
            "Marketplace.AlreadyAMember",
            "User is already a member of this marketplace",
            Error.ErrorType.CONFLICT
    );

    public static final Error CANNOT_REMOVE_OWNER = new Error(
            "Marketplace.CannotRemoveOwner",
            "Cannot remove the marketplace owner",
            Error.ErrorType.VALIDATION
    );

    public static final Error CANNOT_CHANGE_OWNER_ROLE = new Error(
            "Marketplace.CannotChangeOwnerRole",
            "Cannot change the owner's role. Transfer ownership instead.",
            Error.ErrorType.VALIDATION
    );

    public static final Error OWNER_CANNOT_LEAVE = new Error(
            "Marketplace.OwnerCannotLeave",
            "Owner cannot leave the marketplace. Transfer ownership first.",
            Error.ErrorType.VALIDATION
    );

    public static final Error CANNOT_REMOVE_SELF = new Error(
            "Marketplace.CannotRemoveSelf",
            "You cannot remove yourself. Use leave instead.",
            Error.ErrorType.VALIDATION
    );

    // === Role Errors ===

    public static final Error ROLE_NOT_FOUND = new Error(
            "Marketplace.RoleNotFound",
            "Role not found",
            Error.ErrorType.NOT_FOUND
    );

    public static final Error INVALID_ROLE_TYPE = new Error(
            "Marketplace.InvalidRoleType",
            "Invalid role type",
            Error.ErrorType.VALIDATION
    );

    public static final Error CANNOT_ASSIGN_OWNER_ROLE = new Error(
            "Marketplace.CannotAssignOwnerRole",
            "Cannot assign owner role directly. Use transfer ownership.",
            Error.ErrorType.VALIDATION
    );

    // === Invite Errors ===

    public static final Error INVITE_NOT_FOUND = new Error(
            "Marketplace.InviteNotFound",
            "Invite not found",
            Error.ErrorType.NOT_FOUND
    );

    public static final Error INVITE_EXPIRED = new Error(
            "Marketplace.InviteExpired",
            "This invite has expired",
            Error.ErrorType.VALIDATION
    );

    public static final Error INVITE_ALREADY_USED = new Error(
            "Marketplace.InviteAlreadyUsed",
            "This invite has already been used",
            Error.ErrorType.VALIDATION
    );

    public static final Error INVITE_CANCELLED = new Error(
            "Marketplace.InviteCancelled",
            "This invite has been cancelled",
            Error.ErrorType.VALIDATION
    );

    public static final Error PENDING_INVITE_EXISTS = new Error(
            "Marketplace.PendingInviteExists",
            "A pending invite already exists for this user",
            Error.ErrorType.CONFLICT
    );

    public static final Error CANNOT_INVITE_SELF = new Error(
            "Marketplace.CannotInviteSelf",
            "You cannot invite yourself",
            Error.ErrorType.VALIDATION
    );

    public static final Error NOT_INVITE_RECIPIENT = new Error(
            "Marketplace.NotInviteRecipient",
            "This invite is not for you",
            Error.ErrorType.VALIDATION
    );

    // === Transfer Ownership Errors ===

    public static final Error TRANSFER_TARGET_NOT_MEMBER = new Error(
            "Marketplace.TransferTargetNotMember",
            "Target user must be an existing member",
            Error.ErrorType.VALIDATION
    );

    public static final Error CANNOT_TRANSFER_TO_SELF = new Error(
            "Marketplace.CannotTransferToSelf",
            "Cannot transfer ownership to yourself",
            Error.ErrorType.VALIDATION
    );

    // === User Errors ===

    public static final Error USER_NOT_FOUND = new Error(
            "Marketplace.UserNotFound",
            "User not found",
            Error.ErrorType.NOT_FOUND
    );
}