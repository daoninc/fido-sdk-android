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

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

/** @noinspection unused, unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused */
public class PatternParameters {

    private int maxInvalidAttempts = 3;
    private int timeBetweenInvalidAttempts = 15 * 1000;
    private int delayBetweenCapture = 1000;

    // Vibration
    private boolean vibrateOnInvalid = true;
    private int invalidVibrateInterval = 200;

    private boolean showGridlines = false;
    private int gridLineWidth = 5;

    // Details of the pattern
    private float dotInnerRadiusRatio = (float) 0.20;
    private float dotOuterRadiusRatio = (float) 0.40;
    private float selectedDotInnerRadiusRatio = (float) 0.20;
    private float selectedDotOuterRadiusRatio = (float) 1.0;

    // Selection line
    private float patternLineWidth = 20;

    // Security Parameters
    private boolean showPatternLine = true;
    private boolean showSelectedTouchPoints = true;

    // Colors
    private int drawPaintColor = Color.BLUE;
    private int tempDrawPaintColor = Color.BLUE;
    private int gridLineColor = Color.BLUE;

    private int touchPointPaintColor = Color.WHITE;
    private int outerTouchPointPaintColor = Color.LTGRAY;
    private int linePaintColor = Color.DKGRAY;

    private int positiveTouchPointPaintColor = Color.GREEN;
    private int positiveOuterTouchPointPaintColor = 0x40009900;
    private int positiveLinePaintColor = Color.GREEN;

    private int negativeTouchPointPaintColor = Color.RED;
    private int negativeOuterTouchPointPaintColor = 0x40FF0000;
    private int negativeLinePaintColor = Color.RED;

    private int textSize = 12;
    private int textColor = Color.BLACK;
    private String fontFamily;
    private int textFontStyle = 0;

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

    @SuppressLint("WrongConstant")
    public Paint getTextPaint() {

        if (textPaint == null) {
            textPaint = new Paint();
            textPaint.setTextSize(this.getTextSize());
            textPaint.setColor(this.getTextColor());
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTypeface(Typeface.create(this.getFontFamily(), this.getTextFontStyle()));
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

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public int getTextFontStyle() {
        return textFontStyle;
    }

    public void setTextFontStyle(int textFontStyle) {
        this.textFontStyle = textFontStyle;
    }

    public boolean showGridlines() {
        return showGridlines;
    }

    public void setShowGridlines(boolean showGridlines) {
        this.showGridlines = showGridlines;
    }

    public int getGridLineWidth() {
        return gridLineWidth;
    }

    public void setGridLineWidth(int gridLineWidth) {
        this.gridLineWidth = gridLineWidth;
    }

    public float getDotInnerRadiusRatio() {
        return dotInnerRadiusRatio;
    }

    public void setDotInnerRadiusRatio(float dotInnerRadiusRatio) {
        this.dotInnerRadiusRatio = dotInnerRadiusRatio;
    }

    public float getDotOuterRadiusRatio() {
        return dotOuterRadiusRatio;
    }

    public void setDotOuterRadiusRatio(float dotOuterRadiusRatio) {
        this.dotOuterRadiusRatio = dotOuterRadiusRatio;
    }

    public float getSelectedDotInnerRadiusRatio() {
        return selectedDotInnerRadiusRatio;
    }

    public void setSelectedDotInnerRadiusRatio(float selectedDotInnerRadiusRatio) {
        this.selectedDotInnerRadiusRatio = selectedDotInnerRadiusRatio;
    }

    public float getSelectedDotOuterRadiusRatio() {
        return selectedDotOuterRadiusRatio;
    }

    public void setSelectedDotOuterRadiusRatio(float selectedDotOuterRadiusRatio) {
        this.selectedDotOuterRadiusRatio = selectedDotOuterRadiusRatio;
    }

    public float getPatternLineWidth() {
        return patternLineWidth;
    }

    public void setPatternLineWidth(float patternLineWidth) {
        this.patternLineWidth = patternLineWidth;
    }

    public boolean isShowPatternLine() {
        return showPatternLine;
    }

    public void setShowPatternLine(boolean showPatternLine) {
        this.showPatternLine = showPatternLine;
    }

    public boolean isShowSelectedTouchPoints() {
        return showSelectedTouchPoints;
    }

    public void setShowSelectedTouchPoints(boolean showSelectedTouchPoints) {
        this.showSelectedTouchPoints = showSelectedTouchPoints;
    }

    public int getTouchPointPaintColor() {
        return touchPointPaintColor;
    }

    public void setTouchPointPaintColor(int touchPointPaintColor) {
        this.touchPointPaintColor = touchPointPaintColor;
    }

    public int getOuterTouchPointPaintColor() {
        return outerTouchPointPaintColor;
    }

    public void setOuterTouchPointPaintColor(int outerTouchPointPaintColor) {
        this.outerTouchPointPaintColor = outerTouchPointPaintColor;
    }

    public int getLinePaintColor() {
        return linePaintColor;
    }

    public void setLinePaintColor(int linePaintColor) {
        this.linePaintColor = linePaintColor;
    }

    public int getPositiveTouchPointPaintColor() {
        return positiveTouchPointPaintColor;
    }

    public void setPositiveTouchPointPaintColor(int positiveTouchPointPaintColor) {
        this.positiveTouchPointPaintColor = positiveTouchPointPaintColor;
    }

    public int getPositiveOuterTouchPointPaintColor() {
        return positiveOuterTouchPointPaintColor;
    }

    public void setPositiveOuterTouchPointPaintColor(int positiveOuterTouchPointPaintColor) {
        this.positiveOuterTouchPointPaintColor = positiveOuterTouchPointPaintColor;
    }

    public int getPositiveLinePaintColor() {
        return positiveLinePaintColor;
    }

    public void setPositiveLinePaintColor(int positiveLinePaintColor) {
        this.positiveLinePaintColor = positiveLinePaintColor;
    }

    public int getNegativeTouchPointPaintColor() {
        return negativeTouchPointPaintColor;
    }

    public void setNegativeTouchPointPaintColor(int negativeTouchPointPaintColor) {
        this.negativeTouchPointPaintColor = negativeTouchPointPaintColor;
    }

    public int getNegativeOuterTouchPointPaintColor() {
        return negativeOuterTouchPointPaintColor;
    }

    public void setNegativeOuterTouchPointPaintColor(int negativeOuterTouchPointPaintColor) {
        this.negativeOuterTouchPointPaintColor = negativeOuterTouchPointPaintColor;
    }

    public int getNegativeLinePaintColor() {
        return negativeLinePaintColor;
    }

    public void setNegativeLinePaintColor(int negativeeLinePaintColor) {
        this.negativeLinePaintColor = negativeeLinePaintColor;
    }

    public int getDrawPaintColor() {
        return drawPaintColor;
    }

    public void setDrawPaintColor(int drawPaintColor) {
        this.drawPaintColor = drawPaintColor;
    }

    public int getTempDrawPaintColor() {
        return tempDrawPaintColor;
    }

    public void setTempDrawPaintColor(int tempDrawPaintColor) {
        this.tempDrawPaintColor = tempDrawPaintColor;
    }

    public int getGridLineColor() {
        return gridLineColor;
    }

    public void setGridLineColor(int gridLineColor) {
        this.gridLineColor = gridLineColor;
    }

    public int getDelayBetweenCapture() {
        return delayBetweenCapture;
    }

    public void setDelayBetweenCapture(int delayBetweenCapture) {
        this.delayBetweenCapture = delayBetweenCapture;
    }

    public int getMaxInvalidAttempts() {
        return maxInvalidAttempts;
    }

    public void setMaxInvalidAttempts(int maxInvalidAttempts) {
        this.maxInvalidAttempts = maxInvalidAttempts;
    }

    public int getTimeBetweenInvalidAttempts() {
        return timeBetweenInvalidAttempts;
    }

    public void setTimeBetweenInvalidAttempts(int timeBetweenInvalidAttempts) {
        this.timeBetweenInvalidAttempts = timeBetweenInvalidAttempts;
    }

    public boolean isVibrateOnInvalid() {
        return vibrateOnInvalid;
    }

    public void setVibrateOnInvalid(boolean vibrateOnInvalid) {
        this.vibrateOnInvalid = vibrateOnInvalid;
    }

    public int getInvalidVibrateInterval() {
        return invalidVibrateInterval;
    }

    public void setInvalidVibrateInterval(int invalidVibrateInterval) {
        this.invalidVibrateInterval = invalidVibrateInterval;
    }
}
