package org.androidtown.sleepdrive_cognition.googleapi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;



class Mypainter extends View {
    public Mypainter(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
      //  paint.setColor(Color.RED);
      //  paint.setAntiAlias(true);
      //  paint.setAlpha(50);
      //  canvas.drawRect(0,00,400,400,paint);
     //   r1.setBackgroundColor(Color.RED);

    }
}