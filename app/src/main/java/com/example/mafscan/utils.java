package com.example.mafscan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class utils{

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
}

