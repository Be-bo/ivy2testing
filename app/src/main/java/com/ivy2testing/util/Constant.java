package com.ivy2testing.util;

/**
 * Class made to contain all constant values used in source code only
 * Note: not including degree and domain lists (maybe add later?)
 */
public final class Constant {

    public static final String DEFAULT_UNI = "ucalgary.ca";

    // Request Codes
    public static final int PICK_IMAGE_REQUEST_CODE = 101;
    public static final int EDIT_STUDENT_REQUEST_CODE = 102;
    public static final int LOGIN_REQUEST_CODE = 103;
    public static final int VIEW_POST_REQUEST_CODE = 104;
    public static final int USER_PROFILE_REQUEST_CODE = 105;
    public static final int SEEALL_USERS_REQUEST_CODE = 106;
    public static final int SEEALL_POSTS_REQUEST_CODE = 107;
    public static final int EDIT_ORGANIZATION_REQUEST_CODE = 108;
    public static final int CREATE_POST_REQUEST_CODE = 109;


    public static final int PROFILE_POST_GRID_ROW_COUNT = 3;
    public static final int PROFILE_MEMBER_LIMIT = 5;
    public static final int PROFILE_POST_LIMIT_STUDENT = 9;
    public static final int PROFILE_POST_LIMIT_ORG = 6;
    public static final int FEED_ADAPTER_SEEALL = 13;
    public static final int FEED_ADAPTER_CAMPUS = 14;
    public static final int FEED_ADAPTER_EVENTS = 15;
    public static final int NOTIFICATION_CENTER_LIMIT = 15;

    public static final int NOTIFICATION_TYPE_CHAT = 1;
    public static final int NOTIFICATION_TYPE_COMMENT = 2;
    public static final int NOTIFICATION_TYPE_FEATURED = 3;
    public static final int NOTIFICATION_TYPE_ORG_EVENT = 4;
    public static final int NOTIFICATION_TYPE_ORG_POST = 5;
    public static final int NOTIFICATION_BATCH_TOLERANCE = 5;
}
