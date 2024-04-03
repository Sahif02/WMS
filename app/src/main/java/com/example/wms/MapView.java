package com.example.wms;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.example.wms.locate.Node;

public class MapView extends View {

    private Node[][] warehouse;
    private int cellSize; // Size of each cell in pixels

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setWarehouse(Node[][] warehouse, int cellSize) {
        this.warehouse = warehouse;
        this.cellSize = cellSize;
        invalidate(); // Trigger redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (warehouse == null) {
            return;
        }

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        for (int y = 0; y < warehouse.length; y++) {
            for (int x = 0; x < warehouse[y].length; x++) {
                Node cellValue = warehouse[y][x];
                float left = x * cellSize;
                float top = y * cellSize;
                float right = left + cellSize;
                float bottom = top + cellSize;

                switch (cellValue.getType()) {
                    case Node.EMPTY: // Empty space
                        paint.setColor(Color.WHITE);
                        break;
                    case Node.WALL: // Wall
                        paint.setColor(Color.BLACK);
                        break;
                    case Node.START: // Start
                        paint.setColor(Color.GREEN);
                        break;
                    case Node.FINISH: // Finish
                        paint.setColor(Color.RED);
                        break;
                    case Node.OPENED:
                        paint.setColor(Color.WHITE);
                        break;
                    case Node.CLOSED: // Finish
                        paint.setColor(Color.WHITE);
                        break;
                    case  Node.PATH:
                        paint.setColor(Color.GREEN);
                        break;
                }

                canvas.drawRect(left, top, right, bottom, paint);
            }
        }
    }
}