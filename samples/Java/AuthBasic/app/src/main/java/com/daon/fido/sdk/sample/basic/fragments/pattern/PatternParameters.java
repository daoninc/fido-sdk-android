// Copyright (C) 2022 Daon.
//
// Permission to use, copy, modify, and/or distribute this software for any purpose with or without
// fee is hereby granted.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS
// SOFTWARE INCLUDING ALL IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
// SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
// DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER
// TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

package com.daon.fido.sdk.sample.basic.fragments.pattern;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

/**
 * @noinspection unused, FieldCanBeLocal
 */
public class PatternParameters {
    private final int delayBetweenCapture = 1000;

    // Vibration
    private final boolean vibrateOnInvalid = true;
    private final int invalidVibrateInterval = 200;

    private final boolean showGridlines = false;

    // Details of the pattern
    private final float dotInnerRadiusRatio = (float) 0.20;
    private final float dotOuterRadiusRatio = (float) 0.40;
    private final float selectedDotInnerRadiusRatio = (float) 0.20;
    private final float selectedDotOuterRadiusRatio = (float) 1.0;

    // Selection line
    private final float patternLineWidth = 20;

    // Colors
    private final int tempDrawPaintColor = Color.BLUE;
    private final int gridLineColor = Color.BLUE;

    private final int touchPointPaintColor = Color.WHITE;
    private final int outerTouchPointPaintColor = Color.LTGRAY;
    private final int linePaintColor = Color.DKGRAY;

    private final int positiveTouchPointPaintColor = Color.GREEN;
    private final int positiveOuterTouchPointPaintColor = 0x40009900;
    private final int positiveLinePaintColor = Color.GREEN;

    private final int negativeTouchPointPaintColor = Color.RED;
    private final int negativeOuterTouchPointPaintColor = 0x40FF0000;
    private final int negativeLinePaintColor = Color.RED;

    private final int textSize = 12;
    private final int textColor = Color.BLACK;
    private String fontFamily;
    // Paints
    private Paint textPaint;
    private Paint tempDrawPaint;
    private Paint gridLinePaint;

    private Paint touchPointPaint;
    private Paint outerTouchPointPaint;
    private Paint linePaint;

    private Paint positiveTouchPointPaint;
    private Paint positiveOuterTouchPointPaint;
    private Paint positiveLinePaint;

    private Paint negativeTouchPointPaint;
    private Paint negativeOuterTouchPointPaint;
    private Paint negativeLinePaint;

    public PatternParameters() {
    }

    public Paint getTextPaint() {

        if (textPaint == null) {
            textPaint = new Paint();
            textPaint.setTextSize(this.getTextSize());
            textPaint.setColor(this.getTextColor());
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTypeface(Typeface.create(this.getFontFamily(), Typeface.NORMAL));
        }
        return textPaint;
    }

    public Paint getGridLinePaint() {

        if (gridLinePaint == null) {
            gridLinePaint = this.createLinePaint();
            gridLinePaint.setColor(this.getGridLineColor());
        }
        return gridLinePaint;
    }

    public Paint getTempDrawPaint() {

        if (tempDrawPaint == null) {
            tempDrawPaint = this.createLinePaint();
            tempDrawPaint.setColor(this.getTempDrawPaintColor());
        }
        return tempDrawPaint;
    }

    public Paint getTouchPointPaint() {

        if (touchPointPaint == null) {
            touchPointPaint = this.createTouchPointPaint();
            touchPointPaint.setColor(this.getTouchPointPaintColor());
        }
        return touchPointPaint;
    }

    public Paint getOuterTouchPointPaint() {

        if (outerTouchPointPaint == null) {
            outerTouchPointPaint = this.createTouchPointPaint();
            outerTouchPointPaint.setColor(this.getOuterTouchPointPaintColor());
        }
        return outerTouchPointPaint;
    }

    public Paint getLinePaint() {

        if (linePaint == null) {
            linePaint = this.createLinePaint();
            linePaint.setColor(this.getLinePaintColor());
        }
        return linePaint;
    }

    public Paint getPositiveTouchPointPaint() {

        if (positiveTouchPointPaint == null) {
            positiveTouchPointPaint = this.createTouchPointPaint();
            positiveTouchPointPaint.setColor(this.getPositiveTouchPointPaintColor());
        }
        return positiveTouchPointPaint;
    }

    public Paint getPositiveOuterTouchPointPaint() {

        if (positiveOuterTouchPointPaint == null) {
            positiveOuterTouchPointPaint = this.createTouchPointPaint();
            positiveOuterTouchPointPaint.setColor(this.getPositiveOuterTouchPointPaintColor());
        }
        return positiveOuterTouchPointPaint;
    }

    public Paint getPositiveLinePaint() {

        if (positiveLinePaint == null) {
            positiveLinePaint = this.createLinePaint();
            positiveLinePaint.setColor(this.getPositiveLinePaintColor());
        }
        return positiveLinePaint;
    }

    public Paint getNegativeTouchPointPaint() {

        if (negativeTouchPointPaint == null) {
            negativeTouchPointPaint = this.createTouchPointPaint();
            negativeTouchPointPaint.setColor(this.getNegativeTouchPointPaintColor());
        }
        return negativeTouchPointPaint;
    }

    public Paint getNegativeOuterTouchPointPaint() {

        if (negativeOuterTouchPointPaint == null) {
            negativeOuterTouchPointPaint = this.createTouchPointPaint();
            negativeOuterTouchPointPaint.setColor(this.getNegativeOuterTouchPointPaintColor());
        }
        return negativeOuterTouchPointPaint;
    }

    public Paint getNegativeLinePaint() {

        if (negativeLinePaint == null) {
            negativeLinePaint = this.createLinePaint();
            negativeLinePaint.setColor(this.getNegativeLinePaintColor());
        }
        return negativeLinePaint;
    }

    protected Paint createLinePaint() {

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(this.getPatternLineWidth());
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    protected Paint createTouchPointPaint() {

        return new Paint();
    }

    public int getTextSize() {
        return textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public boolean showGridlines() {
        return showGridlines;
    }

    public float getDotInnerRadiusRatio() {
        return dotInnerRadiusRatio;
    }

    public float getDotOuterRadiusRatio() {
        return dotOuterRadiusRatio;
    }

    public float getSelectedDotInnerRadiusRatio() {
        return selectedDotInnerRadiusRatio;
    }

    public float getSelectedDotOuterRadiusRatio() {
        return selectedDotOuterRadiusRatio;
    }

    public float getPatternLineWidth() {
        return patternLineWidth;
    }

    public int getTouchPointPaintColor() {
        return touchPointPaintColor;
    }

    public int getOuterTouchPointPaintColor() {
        return outerTouchPointPaintColor;
    }

    public int getLinePaintColor() {
        return linePaintColor;
    }

    public int getPositiveTouchPointPaintColor() {
        return positiveTouchPointPaintColor;
    }

    public int getPositiveOuterTouchPointPaintColor() {
        return positiveOuterTouchPointPaintColor;
    }

    public int getPositiveLinePaintColor() {
        return positiveLinePaintColor;
    }

    public int getNegativeTouchPointPaintColor() {
        return negativeTouchPointPaintColor;
    }

    public int getNegativeOuterTouchPointPaintColor() {
        return negativeOuterTouchPointPaintColor;
    }

    public int getNegativeLinePaintColor() {
        return negativeLinePaintColor;
    }

    public int getTempDrawPaintColor() {
        return tempDrawPaintColor;
    }


    public int getGridLineColor() {
        return gridLineColor;
    }

    public int getDelayBetweenCapture() {
        return delayBetweenCapture;
    }

    public boolean isVibrateOnInvalid() {
        return vibrateOnInvalid;
    }

    public int getInvalidVibrateInterval() {
        return invalidVibrateInterval;
    }

}
