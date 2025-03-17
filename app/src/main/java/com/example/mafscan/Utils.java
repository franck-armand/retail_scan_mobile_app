package com.example.mafscan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.net.ParseException;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();
    public static class SwipeToDeleteCallback extends
            ItemTouchHelper.SimpleCallback {

        public interface SwipeToDeleteListener {
            void onDelete(int position);
        }
        public interface SwipeToUpdateListener {
            void onUpdate(int position);
        }

        private final SwipeToDeleteListener mDeleteListener;
        private final SwipeToUpdateListener mUpdateListener;
        private final Drawable iconDelete;
        private final Drawable iconUpdate;
        private final ColorDrawable backgroundDelete;
        private final ColorDrawable backgroundUpdate;

        public SwipeToDeleteCallback(Context context,
                                     SwipeToDeleteListener deleteListener,
                                     SwipeToUpdateListener updateListener) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            mDeleteListener = deleteListener;
            mUpdateListener = updateListener;
            iconDelete = ContextCompat.getDrawable(context, R.drawable.outline_delete_forever_24_w);
            iconUpdate = ContextCompat.getDrawable(context, R.drawable.baseline_edit_note_24);
            backgroundDelete = new ColorDrawable(Color.RED);
            backgroundUpdate = new ColorDrawable(Color.BLUE);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false; // We don't support drag and drop
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            if(direction == ItemTouchHelper.LEFT) {
                mDeleteListener.onDelete(position);
            } else if(direction == ItemTouchHelper.RIGHT) {
                mUpdateListener.onUpdate(position);
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20; // So background is behind the rounded corners of itemView

            int iconMargin = (itemView.getHeight() - iconDelete.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - iconDelete.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + iconDelete.getIntrinsicHeight();

            if (dX > 0) { // Swiping to the right
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = itemView.getLeft() + iconMargin + iconUpdate.getIntrinsicWidth();
                iconUpdate.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                backgroundUpdate.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                        itemView.getBottom());
                backgroundUpdate.draw(c);
                iconUpdate.draw(c);

            } else if (dX < 0) { // Swiping to the left
                int iconLeft = itemView.getRight() - iconMargin - iconDelete.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                iconDelete.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                backgroundDelete.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
                backgroundDelete.draw(c);
                iconDelete.draw(c);

            } else { // view is unSwiped
                backgroundDelete.setBounds(0, 0, 0, 0);
                backgroundUpdate.setBounds(0, 0, 0, 0);
            }
        }
    }
    public static String getAppVersion(Context context) {
        try {

            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Error getting app version", e);
            return "Unknown";
        }
    }

    public static List<Map<String, Object>> formatScanData(List<?> dataList) {
        List<Map<String, Object>> formattedData = new ArrayList<>();
        for (Object item : dataList) {
            Map<String, Object> data = new HashMap<>();
            if (item instanceof ScanData) {
                ScanData scanData = (ScanData) item;
                data.put("scannedData", scanData.getScannedData());
                data.put("codeType", scanData.getCodeType());
                data.put("scanCount", scanData.getScanCount());
                data.put("scanDate", scanData.getFormattedScanDate());
                data.put("deviceSerialNumber", DataLogicUtils.getDeviceInfo());
//                data.put("fromLocationId", fromLocationId);
//                data.put("toLocationId", toLocationId);
//                data.put("sessionId", sessionId);
            } else if (item instanceof ScanRecord) {
                ScanRecord scanRecord = (ScanRecord) item;
                data.put("sessionId", scanRecord.sessionId);
                data.put("scannedData", scanRecord.scannedData);
                data.put("scanCount", scanRecord.scanCount);
                data.put("fromLocationId", scanRecord.fromLocationId);
                data.put("toLocationId", scanRecord.toLocationId);
                data.put("scanDate", scanRecord.scanDate);
                data.put("codeType", scanRecord.codeType);
                data.put("deviceSerialNumber", DataLogicUtils.getDeviceInfo());
            }
            formattedData.add(data);
        }
        return formattedData;
    }

    public static void parseDateSqlServerFormat(Map<String, Object> data, PreparedStatement statement)
            throws java.sql.SQLException {
        try {
            String datePattern = "yyyy-MM-dd HH:mm:ss:SSS";
            SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern,
                    Locale.getDefault());
            Date scanDate = dateFormat.parse((String) Objects.requireNonNull(data.get("scanDate")));
            assert scanDate != null;
            statement.setTimestamp(4, new Timestamp(scanDate.getTime()));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + e.getMessage());
        } catch (java.text.ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showToast(Context context, String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    @SuppressLint("RestrictedApi")
    public static void inflateMenu(AppCompatActivity activity, Menu menu, int menuResId) {
        MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(menuResId, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder menuBuilder = (MenuBuilder) menu;
            menuBuilder.setOptionalIconsVisible(true);
        }
    }

    public static void showHelpDialog(FragmentManager fragmentManager) {
        HelpDialogFragment dialog = new HelpDialogFragment();
        dialog.show(fragmentManager, "ScanCodeHelpDialogFragment");
    }
}

