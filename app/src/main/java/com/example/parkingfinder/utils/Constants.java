package com.example.parkingfinder.utils;

/**
 * Utility class containing application-wide constants
 */
public class Constants {

    /**
     * Firebase constants
     */
    public static final class Firebase {
        // Firestore collections
        public static final String COLLECTION_USERS = "users";
        public static final String COLLECTION_PARKING_AREAS = "parking_areas";
        public static final String COLLECTION_PARKING_SPOTS = "parking_spots";
        public static final String COLLECTION_BOOKINGS = "bookings";
        public static final String COLLECTION_REVIEWS = "reviews";
        public static final String COLLECTION_TRANSACTIONS = "transactions";

        // Storage paths
        public static final String STORAGE_PROFILE_IMAGES = "profile_images";
        public static final String STORAGE_PARKING_IMAGES = "parking_images";
    }

    /**
     * Booking status constants
     */
    public static final class BookingStatus {
        public static final String PENDING = "PENDING";
        public static final String CONFIRMED = "CONFIRMED";
        public static final String ACTIVE = "ACTIVE";
        public static final String COMPLETED = "COMPLETED";
        public static final String CANCELLED = "CANCELLED";
        public static final String EXPIRED = "EXPIRED";
    }

    /**
     * Payment method constants
     */
    public static final class PaymentMethod {
        public static final String CREDIT_CARD = "CREDIT_CARD";
        public static final String DEBIT_CARD = "DEBIT_CARD";
        public static final String PAYPAL = "PAYPAL";
        public static final String GOOGLE_PAY = "GOOGLE_PAY";
        public static final String WALLET = "WALLET";
    }

    /**
     * Intent extras
     */
    public static final class IntentExtra {
        public static final String EXTRA_BOOKING_ID = "booking_id";
        public static final String EXTRA_PARKING_AREA_ID = "parking_area_id";
        public static final String EXTRA_PARKING_AREA_NAME = "parking_area_name";
        public static final String EXTRA_PARKING_SPOT_ID = "parking_spot_id";
        public static final String EXTRA_PARKING_SPOT_NUMBER = "parking_spot_number";
        public static final String EXTRA_HOURLY_RATE = "hourly_rate";
        public static final String EXTRA_USER_ID = "user_id";
        public static final String EXTRA_USER_NAME = "user_name";
        public static final String EXTRA_EXTEND_BOOKING_ID = "extend_booking_id";
        public static final String EXTRA_CURRENT_END_TIME = "current_end_time";
        public static final String EXTRA_AUTO_BOOK = "auto_book";
        public static final String EXTRA_OPEN_REGISTER = "open_register";
        public static final String EXTRA_SHOW_EXTEND_OPTION = "show_extend_option";
        public static final String EXTRA_NAVIGATE_TO_BOOKINGS = "navigate_to_bookings";
    }

    /**
     * Shared preferences
     */
    public static final class Preferences {
        public static final String PREFS_NAME = "parking_finder_prefs";
        public static final String PREF_FIRST_LAUNCH = "first_launch";
        public static final String PREF_USER_ID = "user_id";
        public static final String PREF_USER_EMAIL = "user_email";
        public static final String PREF_LAST_LATITUDE = "last_latitude";
        public static final String PREF_LAST_LONGITUDE = "last_longitude";
        public static final String PREF_SEARCH_RADIUS = "search_radius";
        public static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";
        public static final String PREF_LOCATION_TRACKING_ENABLED = "location_tracking_enabled";
    }

    /**
     * Notification constants
     */
    public static final class Notification {
        public static final int REQUEST_CODE_BOOKING_REMINDER = 1001;
        public static final int REQUEST_CODE_BOOKING_START = 1002;
        public static final int REQUEST_CODE_BOOKING_END = 1003;
        public static final int REQUEST_CODE_NEARBY_PARKING = 2001;
        public static final int REQUEST_CODE_PROMOTION = 3001;
    }

    /**
     * Location constants
     */
    public static final class Location {
        public static final long UPDATE_INTERVAL = 10000; // 10 seconds
        public static final long FASTEST_INTERVAL = 5000; // 5 seconds
        public static final float SMALLEST_DISPLACEMENT = 10; // 10 meters
        public static final float DEFAULT_SEARCH_RADIUS = 5.0f; // 5 km
        public static final float MAX_SEARCH_RADIUS = 15.0f; // 15 km
    }

    /**
     * Request codes for activity results
     */
    public static final class RequestCode {
        public static final int LOCATION_PERMISSION_REQUEST = 1001;
        public static final int PICK_IMAGE_REQUEST = 2001;
        public static final int PAYMENT_REQUEST = 3001;
        public static final int LOGIN_REQUEST = 4001;
    }

    /**
     * Work manager tags
     */
    public static final class WorkManager {
        public static final String BOOKING_NOTIFICATION_WORKER = "booking_notifications";
        public static final String PERIODIC_SYNC_WORKER = "periodic_sync";
        public static final String LOCATION_TRACKING_WORKER = "location_tracking";
    }

    /**
     * Time constants (in milliseconds)
     */
    public static final class Time {
        public static final long MINUTE = 60 * 1000; // 1 minute
        public static final long HOUR = 60 * MINUTE; // 1 hour
        public static final long DAY = 24 * HOUR; // 1 day
        public static final long WEEK = 7 * DAY; // 1 week

        public static final long BOOKING_REMINDER_BEFORE = 30 * MINUTE; // 30 minutes
        public static final long BOOKING_END_REMINDER_BEFORE = 30 * MINUTE; // 30 minutes
        public static final long BOOKING_EXPIRED_GRACE_PERIOD = 15 * MINUTE; // 15 minutes
    }
}