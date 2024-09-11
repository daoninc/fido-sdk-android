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
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.daon.sdk.authenticator.ErrorCodes;
import com.daon.sdk.authenticator.controller.AuthenticatorError;
import com.daon.sdk.authenticator.controller.PatternControllerProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This is a view used to capture a pattern from the user. The view is divided in 81 rectangles
 * (9x9) and there are 9 defined touch points (1-9).
 *
 * <p>The rules are pretty simple 1. A touch point can only be used once 2. A single continuous
 * pattern is required 3. If a straight line between two points intersects a mid point, then the mid
 * point is included 4. The order, the touch points are recorded is important
 *
 * <p>00 01 02 03 04 05 06 07 08 09 -10- 11 12 -13- 14 15 -16- 17 18 19 20 21 22 23 24 25 26 27 28
 * 29 30 31 32 33 34 35 36 -37- 38 39 -40- 41 42 -43- 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58
 * 59 60 61 62 63 -64- 65 66 -67- 68 69 -70- 71 72 73 74 75 76 77 78 79 80
 *
 * <p>There are two Modes and four States supported by the system.
 *
 * <p>The two Modes are ENROLL and AUTHENTICATE
 *
 * <p>ENROLL The
 * @noinspection unused
 */
public class PatternCollect extends View {

    private static final int NUM_COLS = 9;
    private static final int NUM_ROWS = 9;

    public enum Mode {
        ENROLL, AUTHENTICATE
    }

    public enum Status {
        FIRST_ENROLLMENT_COMPLETE, INVALID_CONFIRMATION_ENROLLMENT, VERIFIED, NOT_VERIFIED, INVALID_ENROLMENT_MIN_SIZE, INVALID_ENROLMENT_MAX_SIZE, INVALID_ENROLMENT_WEAK_PATTERN
    }

    public enum State {
        CAPTURE, FEEDBACK_POSITIVE, FEEDBACK_INVALID_INPUT
    }

    public interface PatternCollectResultReceiver {

        void onPatternCollectResult(PatternCollectResult result);
    }

    public static class PatternCollectResult {

        private Mode mode;
        private Status status;

        public PatternCollectResult() {
        }

        public Mode getMode() {
            return mode;
        }

        public void setMode(Mode aMode) {
            this.mode = aMode;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status aStatus) {
            status = aStatus;
        }
    }

    // Touch points
    private static final List<Integer> TOUCH_POINTS = new ArrayList<>();

    private IPatternManager passcodeManager;
    private PatternParameters parameters = new PatternParameters();
    private Mode currentMode;
    private PatternCollectResultReceiver resultInterface;
    private int[] tempPattern;
    private State currentState = State.CAPTURE;

    // dot radius
    private float dotRadius;
    // dot outer radius
    private float dotOuterRadius;
    // selected dot radius
    private float selectedDotRadius;
    // selected dot outer radius
    private float selectedDotOuterRadius;

    // Touched points
    private final List<Integer> touchedPoints = new ArrayList<>();
    // Current start point
    private int currentPoint = Integer.MIN_VALUE;
    // Temp drawing path
    private Path tempDrawPath;
    // Drawing path
    private Path drawPath;
    // Message
    private String message;
    // Touch boxes
    private final RectF[] rectangles = new RectF[NUM_COLS * NUM_ROWS];

    private final Timer timer = new Timer();

    private PatternControllerProtocol controller;

    // Set the List of touch points from the grid of rectangles
    static {
        int[] touchRectangles = {10, 13, 16, 37, 40, 43, 64, 67, 70};
        for (int touchRect : touchRectangles) {
            TOUCH_POINTS.add(touchRect);
        }
    }

    public PatternCollect(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    public void setParameters(PatternParameters parameters) {
        this.parameters = parameters;
    }

    public void setController(PatternControllerProtocol controller) {
        this.controller = controller;
    }

    /***
     * Start new capture process
     */
    public void startCapture(Mode aMode, PatternCollectResultReceiver resultInterface, PatternParameters parameters, IPatternManager passcodeManager) {

        this.passcodeManager = passcodeManager;
        this.parameters = parameters;
        this.currentMode = aMode;
        this.resultInterface = resultInterface;
        this.internalStartCapture();
    }

    protected void internalStartCapture() {

        currentState = State.CAPTURE;
        setEnabled(true);
        if (!touchedPoints.isEmpty()) {
            clearTheScreen();
        }
    }

    protected void clearTheScreen() {

        tempDrawPath.reset();
        this.touchedPoints.clear();
        currentPoint = Integer.MIN_VALUE;
        drawPath.reset();
        invalidate();
    }

    /**
     * Complete the capture in accordance with the Mode
     *
     * <p>If the user has not touched any touch points, then ignore the touch.
     */
    protected void completeCapture() {

        int[] pattern = convertToPattern();

        if (pattern.length == 0) {
            return;
        }
        if (currentMode == Mode.AUTHENTICATE) {
            completeAuthentication(pattern);
        }
        if (currentMode == Mode.ENROLL) {
            completeEnrollment(pattern);
        }
    }

    protected void completeAuthentication(int[] pattern) {

        final PatternCollectResult result = new PatternCollectResult();
        result.setMode(Mode.AUTHENTICATE);

        passcodeManager.validate(pattern, valid -> {
            if (valid) {
                currentState = State.FEEDBACK_POSITIVE;
                result.setStatus(Status.VERIFIED);
                sendDelayedResponse(result);
            } else {
                currentState = State.FEEDBACK_INVALID_INPUT;
                vibrateDevice();

                result.setStatus(Status.NOT_VERIFIED);
                sendDelayedResponse(result);
            }
        });
    }

    protected void sendDelayedResponse(PatternCollectResult result) {

        setEnabled(false);
        resultInterface.onPatternCollectResult(result);
        delayRecapture();
    }

    protected void delayRecapture() {

        setEnabled(false);
        timer.schedule(new RecaptureTask(), parameters.getDelayBetweenCapture());
    }

    protected void completeEnrollment(int[] pattern) {

        if (tempPattern == null) {
            processFirstEnrolment(pattern);
        } else {
            processEnrolmentConfirmation(pattern);
        }
    }

    protected void processFirstEnrolment(int[] pattern) {

        if (controller != null) {
            AuthenticatorError error = controller.validatePattern(pattern);
            if (error == null) {
                tempPattern = pattern;
                currentState = State.FEEDBACK_POSITIVE;
                PatternCollectResult result = new PatternCollectResult();
                result.setMode(Mode.ENROLL);
                result.setStatus(Status.FIRST_ENROLLMENT_COMPLETE);
                sendDelayedResponse(result);
            } else {
                switch (error.getCode()) {
                    case ErrorCodes.ERROR_PATTERN_TOO_SHORT:
                        processEnrolmentError(Status.INVALID_ENROLMENT_MIN_SIZE);
                        return;
                    case ErrorCodes.ERROR_PATTERN_TOO_LONG:
                        processEnrolmentError(Status.INVALID_ENROLMENT_MAX_SIZE);
                        return;
                    case ErrorCodes.ERROR_PATTERN_WEAK:
                        processEnrolmentError(Status.INVALID_ENROLMENT_WEAK_PATTERN);
                }
            }
        }
    }

    protected void processEnrolmentConfirmation(int[] pattern) {
        if (Arrays.equals(pattern, tempPattern)) {
            clearTempPattern();
            passcodeManager.store(pattern);
            setEnabled(false);
            currentState = State.FEEDBACK_POSITIVE;
        } else {
            // The user has re-entered a different pattern
            currentState = State.FEEDBACK_INVALID_INPUT;
            delayRecapture();
            vibrateDevice();
            PatternCollectResult result = new PatternCollectResult();
            result.setMode(Mode.ENROLL);
            result.setStatus(Status.INVALID_CONFIRMATION_ENROLLMENT);
            sendDelayedResponse(result);
        }
    }

    protected void processEnrolmentError(Status failedStatus) {
        PatternCollectResult result = new PatternCollectResult();
        result.setMode(Mode.ENROLL);
        result.setStatus(failedStatus);
        this.currentState = State.FEEDBACK_INVALID_INPUT;
        this.sendDelayedResponse(result);
        this.vibrateDevice();
    }

    // setup drawing
    private void setupDrawing() {

        drawPath = new Path();
        tempDrawPath = new Path();
    }

    // size assigned to view
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Determine the location of each rectangle on the screen
        int counter = 0;
        int rectWidth = w / NUM_COLS;
        int rectHeight = h / NUM_ROWS;
        for (int y = 0; y < NUM_ROWS; y++) {

            int yPos = (h / NUM_ROWS) * y;
            for (int x = 0; x < NUM_COLS; x++) {

                int xPos = (w / NUM_COLS) * x;
                rectangles[counter++] = new RectF(xPos, yPos, xPos + rectWidth, yPos + rectHeight);
            }
        }

        int widthOfEachSquare = w / NUM_COLS;
        int heightOfEachSquare = h / NUM_ROWS;
        int radius = (Math.min(widthOfEachSquare, heightOfEachSquare)) / 2;

        dotRadius = radius * parameters.getDotInnerRadiusRatio();
        dotOuterRadius = radius * parameters.getDotOuterRadiusRatio();
        selectedDotRadius = radius * parameters.getSelectedDotInnerRadiusRatio();
        selectedDotOuterRadius = radius * parameters.getSelectedDotOuterRadiusRatio();
    }

    // draw the view - will be called after touch event
    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        Paint linePaint = null;
        Paint outerTouchPointPaint = null;
        Paint touchPointPaint = null;
        switch (this.currentState) {
            case CAPTURE:
                linePaint = parameters.getLinePaint();
                outerTouchPointPaint = parameters.getOuterTouchPointPaint();
                touchPointPaint = parameters.getTouchPointPaint();
                break;
            case FEEDBACK_POSITIVE:
                linePaint = parameters.getPositiveLinePaint();
                outerTouchPointPaint = parameters.getPositiveOuterTouchPointPaint();
                touchPointPaint = parameters.getPositiveTouchPointPaint();
                break;
            case FEEDBACK_INVALID_INPUT:
                linePaint = parameters.getNegativeLinePaint();
                outerTouchPointPaint = parameters.getNegativeOuterTouchPointPaint();
                touchPointPaint = parameters.getNegativeTouchPointPaint();
                break;
        }

        for (int x : TOUCH_POINTS) {
            canvas.drawCircle(rectangles[x].centerX(), rectangles[x].centerY(), dotOuterRadius, parameters.getOuterTouchPointPaint());
            canvas.drawCircle(rectangles[x].centerX(), rectangles[x].centerY(), dotRadius, parameters.getTouchPointPaint());
        }
        if (parameters.showGridlines()) {
            for (RectF rect : rectangles) canvas.drawRect(rect, parameters.getGridLinePaint());
        }

        canvas.drawPath(drawPath, linePaint);
        canvas.drawPath(tempDrawPath, parameters.getTempDrawPaint());
        for (Integer aPoint : this.touchedPoints) {
            canvas.drawCircle(rectangles[aPoint].centerX(), rectangles[aPoint].centerY(), selectedDotOuterRadius, outerTouchPointPaint);
            canvas.drawCircle(rectangles[aPoint].centerX(), rectangles[aPoint].centerY(), selectedDotRadius, touchPointPaint);
        }

        if (message != null && !message.isEmpty()) {
            int xPos = (canvas.getWidth() / 2);
            Rect r = new Rect();
            parameters.getTextPaint().getTextBounds(message, 0, message.length(), r);
            int yPos = (canvas.getHeight() / 2) - (Math.abs(r.height())) / 2; // or maybe -= instead of +=, depends on your coordinates
            canvas.drawText(message, xPos, yPos, parameters.getTextPaint());
        }
    }

    // register user touches as drawing action
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // If the view is disabled, ignore the touch
        if (!isEnabled()) {
            return true;
        }

        try {
            float touchX = event.getX();
            float touchY = event.getY();
            int locationRect = determineRectangle(touchX, touchY);

            // Not inside the touch areas - signal the capture is complete
            if (locationRect < 0) {
                if (!tempDrawPath.isEmpty()) {
                    tempDrawPath.reset();
                    completeCapture();
                }
                return true;
            }

            float newX = rectangles[locationRect].centerX();
            float newY = rectangles[locationRect].centerY();

            // respond to down, move and up events
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // If the user has touched one of the touch points, register it otherwise ignore it
                    if (TOUCH_POINTS.contains(locationRect)) {
                        currentPoint = locationRect;
                        touchedPoints.add(currentPoint);
                        drawPath.moveTo(newX, newY);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    // If the user moved to an touch point not touched before
                    if (TOUCH_POINTS.contains(locationRect) && !touchedPoints.contains(locationRect)) {
                        // Is this the first touch point
                        if (this.currentPoint >= 0) {
                            drawPath.lineTo(newX, newY);
                            this.addTouchedPoints(currentPoint, locationRect);
                        } else {
                            drawPath.moveTo(newX, newY);
                            this.addTouchedPoint(locationRect);
                        }
                        tempDrawPath.reset();
                        tempDrawPath.moveTo(newX, newY);
                        currentPoint = locationRect;
                    } else {
                        if (this.currentPoint >= 0) {
                            tempDrawPath.reset();
                            tempDrawPath.moveTo(this.rectangles[currentPoint].centerX(), this.rectangles[currentPoint].centerY());
                            tempDrawPath.lineTo(touchX, touchY);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    tempDrawPath.reset();
                    if (TOUCH_POINTS.contains(locationRect) && !touchedPoints.contains(locationRect)) {
                        this.addTouchedPoints(currentPoint, locationRect);
                        drawPath.lineTo(newX, newY);
                    }
                    this.completeCapture();
                    break;
                default:
                    return false;
            }
            // redraw
        } catch (Throwable ex) {
            Toast.makeText(this.getContext(), ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        invalidate();
        return true;
    }

    protected void vibrateDevice() {

        if (parameters.isVibrateOnInvalid()) {
            Vibrator vibrator = (Vibrator) this.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(parameters.getInvalidVibrateInterval());
            }
        }
    }

    protected void addTouchedPoint(int aPoint) {

        this.conditionallyAdd(aPoint);
    }

    protected void addTouchedPoints(int fromPoint, int toPoint) {

        int rowDistance = Math.abs(this.getRow(fromPoint) - this.getRow(toPoint));
        int columnDistance = Math.abs(this.getColumn(fromPoint) - this.getColumn(toPoint));
        if (rowDistance == 2 || columnDistance == 2) {
            if (rowDistance == 2 && columnDistance == 2) {
                this.conditionallyAdd(40);
            } else {
                if (rowDistance == 0) {
                    switch (this.getRow(fromPoint)) {
                        case 1:
                            this.conditionallyAdd(13);
                            break;
                        case 2:
                            this.conditionallyAdd(40);
                            break;
                        case 3:
                            this.conditionallyAdd(67);
                            break;
                    }
                }
                if (rowDistance == 2 && columnDistance == 0) {
                    switch (this.getColumn(fromPoint)) {
                        case 1:
                            this.conditionallyAdd(37);
                            break;
                        case 2:
                            this.conditionallyAdd(40);
                            break;
                        case 3:
                            this.conditionallyAdd(43);
                            break;
                    }
                }
            }
        }
        this.touchedPoints.add(toPoint);
    }

    protected void conditionallyAdd(int aRectIndex) {

        if (!this.touchedPoints.contains(aRectIndex)) {
            this.touchedPoints.add(aRectIndex);
        }
    }

    /***
     * Converts the array of touched points to an array of 1-9
     */
    protected int[] convertToPattern() {
        int[] pattern = new int[touchedPoints.size()];
        for (int x = 0; x < this.touchedPoints.size(); x++) {
            pattern[x] = TOUCH_POINTS.indexOf(this.touchedPoints.get(x)) + 1;
        }
        return pattern;
    }

    /***
     * Determine from the index of the touched point, the row entry (1-3)
     */
    protected int getRow(int aRectIndex) {

        return switch (aRectIndex) {
            case 10, 13, 16 -> 1;
            case 37, 40, 43 -> 2;
            case 64, 67, 70 -> 3;
            default -> throw new RuntimeException("Invalid touchable rectangle at: " + aRectIndex);
        };
    }

    /***
     * Determine from the index of the touched point, the column entry (1-3)
     */
    protected int getColumn(int aRectIndex) {

        return switch (aRectIndex) {
            case 10, 37, 64 -> 1;
            case 13, 40, 67 -> 2;
            case 16, 43, 70 -> 3;
            default -> throw new RuntimeException("Invalid touchable rectangle at: " + aRectIndex);
        };
    }

    /***
     * Determine which rectangle, if any, the user touched
     */
    protected int determineRectangle(float x, float y) {

        for (int z = 0; z < this.rectangles.length; z++) {
            RectF currentRect = this.rectangles[z];
            if (currentRect.contains(x, y)) {
                return z;
            }
        }

        return -1;
    }

    private class RecaptureTask extends TimerTask {

        RecaptureTask() {
        }

        @Override
        public void run() {
            ((Activity) getContext()).runOnUiThread(() -> {
                setEnabled(true);
                currentState = State.CAPTURE;
                clearTheScreen();
            });
        }
    }

    private void clearTempPattern() {
        if (tempPattern != null) {
            Arrays.fill(tempPattern, 0);
            tempPattern = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        clearTempPattern();
        super.onDetachedFromWindow();
    }
}
