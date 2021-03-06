//
//  Constants.h
//  iOS Data Collector
//
//  Created by Mike Stowell and Jeremy Poulin on 2/28/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#ifndef Constants_h
#define Constants_h

// default sample interval
#define DEFAULT_SAMPLE_INTERVAL      125

// constants for dialogs
#define MENU_PROJECT                  0
#define MENU_LOGIN                    1
#define PROJ_MANUAL                   118
#define CLEAR_FIELDS_DIALOG           3
#define MENU_UPLOAD                   4
#define DESCRIPTION_AUTOMATIC         5
#define MENU_MEDIA_AUTOMATIC          2

// constants for manual dialog
#define MANUAL_MENU_UPLOAD            0
#define MANUAL_MENU_PROJECT           1
#define MANUAL_MENU_LOGIN             2

// options for action sheet
#define OPTION_CANCELED                0
#define OPTION_ENTER_PROJECT_NUMBER    1
#define OPTION_BROWSE_PROJECTS         2
#define OPTION_SCAN_QR_CODE            3

// types of text field data
#define TYPE_DEFAULT   0
#define TYPE_LATITUDE  1
#define TYPE_LONGITUDE 2
#define TYPE_TIME      3

// ipad and iphone dimensions
#define IPAD_WIDTH_PORTRAIT     725
#define IPAD_WIDTH_LANDSCAPE    980
#define IPHONE_WIDTH_PORTRAIT   280
#define IPHONE_WIDTH_LANDSCAPE  415

// manual scrollview drawing constants
#define SCROLLVIEW_Y_OFFSET     50
#define SCROLLVIEW_OBJ_INCR     30
#define SCROLLVIEW_LABEL_HEIGHT 20
#define SCROLLVIEW_TEXT_HEIGHT  35
#define UI_FIELDNAME            0
#define UI_FIELDCONTENTS        1

// manual scrollview oddity patches
#define PORTRAIT_BOTTOM_CUT_IPAD    30
#define PORTRAIT_BOTTOM_CUT_IPHONE  20
#define LANDSCAPE_BOTTOM_CUT_IPAD   1
#define LANDSCAPE_BOTTOM_CUT_IPHONE 80
#define TOP_ELEMENT_ADJUSTMENT      30
#define START_Y_PORTRAIT_IPAD       50
#define START_Y_PORTRAIT_IPHONE     50
#define START_Y_LANDSCAPE_IPAD      40
#define START_Y_LANDSCAPE_IPHONE    0

// manual scrollview keyboard and other offset values
#define KEY_OFFSET_SCROLL_LAND_IPAD     40
#define KEY_OFFSET_SCROLL_PORT_IPHONE   18
#define KEY_OFFSET_FRAME_PORT_IPHONE    18
#define KEY_OFFSET_SCROLL_LAND_IPHONE   60
#define KEY_OFFSET_FRAME_LAND_IPHONE    90
#define KEY_HEIGHT_OFFSET               40
#define RECT_HEIGHT_OFFSET              30


// tags for types of UITextFields in Manual
#define TAG_DEFAULT 1000
#define TAG_TEXT    5000
#define TAG_NUMERIC 6000

// constants for moving Automatic's view up when keyboard present
#define KEY_OFFSET_SESSION_LAND_IPHONE  75
#define KEY_OFFSET_SAMPLE_LAND_IPHONE   75
#define KEY_OFFSET_SAMPLE_PORT_IPHONE   230
#define KEY_OFFSET_SAMPLE_PORT_IPAD     155

// nav controller height
#define NAVIGATION_CONTROLLER_HEIGHT 64

// waffle constants
#define WAFFLE_LENGTH_SHORT  2.0
#define WAFFLE_LENGTH_LONG   3.5
#define WAFFLE_BOTTOM @"bottom"
#define WAFFLE_TOP @"top"
#define WAFFLE_CENTER @"center"
#define WAFFLE_CHECKMARK @"waffle_check"
#define WAFFLE_RED_X @"waffle_x"
#define WAFFLE_WARNING @"waffle_warn"

// data recording constants
#define S_INTERVAL      125
#define TEST_LENGTH     600
#define MAX_DATA_POINTS (1000/S_INTERVAL) * TEST_LENGTH

// step one setup text field tags
#define TAG_STEP1_DATA_SET_NAME     1000
#define TAG_STEP1_SAMPLE_INTERVAL   1001
#define TAG_STEP1_TEST_LENGTH       1002

// delegate constants to determine the calling class
#define DELEGATE_KEY_AUTOMATIC  0
#define DELEGATE_KEY_MANUAL     1
#define DELELGATE_KEY_QUEUE     2

// constants for QueueUploaderView's actionSheet
#define QUEUE_DELETE        0
#define QUEUE_RENAME        1
#define QUEUE_CHANGE_DESC   2
#define QUEUE_SELECT_PROJ   3
#define QUEUE_LOGIN         500

// other character restriction text field tags
#define TAG_QUEUE_RENAME    700
#define TAG_QUEUE_DESC      701
#define TAG_QUEUE_PROJ      702
#define TAG_STEPONE_PROJ    703
#define TAG_AUTO_LOGIN      704
#define TAG_MANUAL_LOGIN    705
#define TAG_MANUAL_PROJ     706

// global proj prefs
#define kPROJECT_ID @"project_id"
#define kPROJECT_ID_DC @"project_id_dc"
#define kPROJECT_ID_MANUAL @"project_id_manual"
#define kENABLE_MANUAL @"enable_manual"

// to get matched fields
#define kFIELD_PREF_STRING @"field_prefs_for_proj_"

// to get the useDev flag
#define kUSE_DEV @"use_dev_flag"

#endif
