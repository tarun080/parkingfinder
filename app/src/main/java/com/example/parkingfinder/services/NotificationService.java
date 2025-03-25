package com.example.parkingfinder.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.parkingfinder.R;
import com.example.parkingfinder.activities.BookingDetailsActivity;
import com.example.parkingfinder.activities.MainActivity;
import com.example.parkingfinder.activities.ParkingDetailsActivity;
import com.example.parkingfinder.database.AppDatabase;
import com.example.parkingfinder.database.entities.BookingEntity;
import com.example.parkingfinder.firebase.FirebaseAuthManager;
import com.example.parkingfinder.models.Booking;
import com.example.parkingfinder.models.ParkingArea;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationService {

    private static final String TAG = "NotificationService";

    // Notification channels
    public static final String CHANNEL_BOOKINGS = "channel_bookings";
    public static final String CHANNEL_PARKING = "channel_parking";
    public static final String CHANNEL_PROMOTIONS = "channel_promotions";

    // Notification IDs
    private static final int NOTIFICATION_BOOKING_UPCOMING = 1001;
    private static final int NOTIFICATION_BOOKING_STARTED = 1002;
    private static final int NOTIFICATION_BOOKING_ENDING = 1003;
    private static final int NOTIFICATION_BOOKING_EXPIRED = 1004;
    private static final int NOTIFICATION_NEARBY_PARKING = 2001;
    private static final int NOTIFICATION_PROMOTION = 3001;

    private Context context;
    private NotificationManager notificationManager;

    public NotificationService(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Bookings channel (high importance)
            NotificationChannel bookingsChannel = new NotificationChannel(
                    CHANNEL_BOOKINGS,
                    "Booking Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            bookingsChannel.setDescription("Notifications about your parking bookings");
            bookingsChannel.enableVibration(true);
            notificationManager.createNotificationChannel(bookingsChannel);

            // Parking channel (default importance)
            NotificationChannel parkingChannel = new NotificationChannel(
                    CHANNEL_PARKING,
                    "Parking Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            parkingChannel.setDescription("Notifications about nearby parking availability");
            notificationManager.createNotificationChannel(parkingChannel);

            // Promotions channel (low importance)
            NotificationChannel promotionsChannel = new NotificationChannel(
                    CHANNEL_PROMOTIONS,
                    "Promotions & Offers",
                    NotificationManager.IMPORTANCE_LOW
            );
            promotionsChannel.setDescription("Promotional offers and discounts");
            promotionsChannel.enableVibration(false);
            notificationManager.createNotificationChannel(promotionsChannel);
        }
    }

    /**
     * Send notification for upcoming booking (30 minutes before start time)
     */
    public void sendUpcomingBookingNotification(Booking booking) {
        if (booking == null) return;

        String title = "Booking Reminder";
        String message = "Your parking at " + booking.getParkingAreaName() + " starts in 30 minutes";

        Intent intent = new Intent(context, BookingDetailsActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        sendNotification(title, message, CHANNEL_BOOKINGS,
                NOTIFICATION_BOOKING_UPCOMING, pendingIntent, true);
    }

    /**
     * Send notification when booking has started
     */
    public void sendBookingStartedNotification(Booking booking) {
        if (booking == null) return;

        String title = "Your Parking Has Started";
        String message = "Your booking at " + booking.getParkingAreaName() + " (Spot " +
                booking.getParkingSpotNumber() + ") is now active";

        Intent intent = new Intent(context, BookingDetailsActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        sendNotification(title, message, CHANNEL_BOOKINGS,
                NOTIFICATION_BOOKING_STARTED, pendingIntent, true);
    }

    /**
     * Send notification when booking is ending soon (30 minutes before end time)
     */
    public void sendBookingEndingSoonNotification(Booking booking) {
        if (booking == null) return;

        String title = "Booking Ending Soon";
        String message = "Your parking at " + booking.getParkingAreaName() + " ends in 30 minutes";

        Intent intent = new Intent(context, BookingDetailsActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("show_extend_option", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        sendNotification(title, message, CHANNEL_BOOKINGS,
                NOTIFICATION_BOOKING_ENDING, pendingIntent, true);
    }

    /**
     * Send notification when booking has expired
     */
    public void sendBookingExpiredNotification(Booking booking) {
        if (booking == null) return;

        String title = "Parking Time Expired";
        String message = "Your booking at " + booking.getParkingAreaName() + " has ended";

        Intent intent = new Intent(context, BookingDetailsActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        sendNotification(title, message, CHANNEL_BOOKINGS,
                NOTIFICATION_BOOKING_EXPIRED, pendingIntent, true);
    }

    /**
     * Send notification about nearby parking availability
     */
    public void sendNearbyParkingNotification(ParkingArea parkingArea, double distance) {
        if (parkingArea == null) return;

        String formattedDistance = String.format("%.1f", distance);
        String title = "Parking Available Nearby";
        String message = parkingArea.getName() + " has " + parkingArea.getAvailableSpots() +
                " spots available just " + formattedDistance + " km away";

        Intent intent = new Intent(context, ParkingDetailsActivity.class);
        intent.putExtra("parking_area_id", parkingArea.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        sendNotification(title, message, CHANNEL_PARKING,
                NOTIFICATION_NEARBY_PARKING, pendingIntent, false);
    }

    /**
     * Send promotional notification
     */
    public void sendPromotionalNotification(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        sendNotification(title, message, CHANNEL_PROMOTIONS,
                NOTIFICATION_PROMOTION, pendingIntent, false);
    }

    /**
     * Helper method to send notifications
     */
    private void sendNotification(String title, String message, String channelId,
                                  int notificationId, PendingIntent pendingIntent, boolean useSound) {
        Uri defaultSoundUri = useSound ?
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : null;

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        if (useSound) {
            notificationBuilder.setSound(defaultSoundUri);
        }

        notificationManager.notify(notificationId, notificationBuilder.build());

        Log.d(TAG, "Notification sent: " + title);
    }

    /**
     * Background worker for checking bookings and sending notifications
     */
    public static class BookingNotificationWorker extends Worker {

        public BookingNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            Context context = getApplicationContext();
            NotificationService notificationService = new NotificationService(context);
            FirebaseAuthManager authManager = FirebaseAuthManager.getInstance();

            // Only proceed if user is logged in
            if (!authManager.isUserLoggedIn()) {
                return Result.success();
            }

            String userId = authManager.getCurrentUser().getUid();

            try {
                // Get upcoming and active bookings from local database
                AppDatabase db = AppDatabase.getDatabase(context);
                List<BookingEntity> bookings = db.bookingDao().getActiveBookingsSync(userId);

                Date now = new Date();
                long thirtyMinutesMs = TimeUnit.MINUTES.toMillis(30);

                for (BookingEntity booking : bookings) {
                    Date startTime = new Date(booking.getStartTime());
                    Date endTime = new Date(booking.getEndTime());

                    // Check if booking is starting in 30 minutes
                    if (now.before(startTime) && startTime.getTime() - now.getTime() <= thirtyMinutesMs) {
                        // Convert to Booking model and send notification
                        notificationService.sendUpcomingBookingNotification(convertToBooking(booking));
                    }

                    // Check if booking just started (within last 5 minutes)
                    if (startTime.before(now) && now.getTime() - startTime.getTime() <= TimeUnit.MINUTES.toMillis(5)) {
                        notificationService.sendBookingStartedNotification(convertToBooking(booking));
                    }

                    // Check if booking is ending in 30 minutes
                    if (now.before(endTime) && endTime.getTime() - now.getTime() <= thirtyMinutesMs) {
                        notificationService.sendBookingEndingSoonNotification(convertToBooking(booking));
                    }

                    // Check if booking just expired (within last 5 minutes)
                    if (endTime.before(now) && now.getTime() - endTime.getTime() <= TimeUnit.MINUTES.toMillis(5)) {
                        notificationService.sendBookingExpiredNotification(convertToBooking(booking));
                    }
                }

                return Result.success();
            } catch (Exception e) {
                Log.e(TAG, "Error checking bookings for notifications", e);
                return Result.failure();
            }
        }

        private Booking convertToBooking(BookingEntity entity) {
            Booking booking = new Booking(
                    entity.getUserId(),
                    entity.getParkingAreaId(),
                    entity.getParkingSpotId(),
                    entity.getParkingAreaName(),
                    entity.getParkingSpotNumber(),
                    new Date(entity.getStartTime()),
                    new Date(entity.getEndTime()),
                    entity.getTotalCost()
            );
            booking.setId(entity.getId());
            booking.setStatus(entity.getStatus());
            booking.setVehicleRegistration(entity.getVehicleRegistration());
            return booking;
        }
    }
}