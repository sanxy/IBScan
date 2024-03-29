
package com.sanxynet.ibscan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.integratedbiometrics.ibscanultimate.IBScan;
import com.integratedbiometrics.ibscanultimate.IBScan.SdkVersion;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.FingerCountState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.FingerQualityState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.ImageData;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.ImageType;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.LedState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.PlatenState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.RollingData;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.SegmentPosition;
import com.integratedbiometrics.ibscanultimate.IBScanDeviceListener;
import com.integratedbiometrics.ibscanultimate.IBScanException;
import com.integratedbiometrics.ibscanultimate.IBScanListener;

import timber.log.Timber;
import uk.co.senab.photoview.PhotoViewAttacher;

public class IbScanActivity extends Activity implements IBScanListener, IBScanDeviceListener {
    /* *********************************************************************************************
     * PRIVATE CONSTANT
     ******************************************************************************************** */

    /* Tag used for Android log messages from this application. */
    private static final String TAG = "IB Scan";

    protected static final int __INVALID_POS__ = -1;

    /*The default value of the TextView state. */
    protected static final String __NFIQ_DEFAULT__ = "0-0-0-0";

    /*The default value of the TextView frame time. */
    protected static final String __NA_DEFAULT__ = "n/a";

    /* The default file name for images and email templates. */
    protected static final String FILE_NAME_DEFAULT = "output";

    /* The quantity of finger qualities set in the preview image. */
    protected static final int FINGER_QUALITIES_COUNT = 4;

    /* The background color of the ImageView preview image. */
    protected static final int PREVIEW_IMAGE_BACKGROUND = Color.LTGRAY;

    /* The background color of a TextView with finger quality when the finger is not present. */
    protected static final int FINGER_QUALITY_NOT_PRESENT_COLOR = Color.LTGRAY;

    /* The background color of a TextView with finger quality when the finger is of good quality. */
    protected static final int FINGER_QUALITY_GOOD_COLOR = Color.GREEN;

    /* The background color of a TextView with finger quality when the finger is of good quality. */
    protected static final int FINGER_QUALITY_FAIR_COLOR = Color.YELLOW;

    /* the background color of a TextView finger quality when the finger is of poor quality. */
    protected static final int FINGER_QUALITY_POOR_COLOR = Color.RED;

    protected final int __TIMER_STATUS_DELAY__ = 500;

    // Capture Sequence Definitions
    protected final String CAPTURE_SEQ_FLAT_SINGLE_FINGER = "Single flat finger";
    protected final String CAPTURE_SEQ_ROLL_SINGLE_FINGER = "Single rolled finger";
    protected final String CAPTURE_SEQ_2_FLAT_FINGERS = "2 flat fingers";
    protected final String CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS = "10 single flat fingers";
    protected final String CAPTURE_SEQ_10_SINGLE_ROLLED_FINGERS = "10 single rolled fingers";
    protected final String CAPTURE_SEQ_4_FLAT_FINGERS = "4 flat fingers";
    protected final String CAPTURE_SEQ_10_FLAT_WITH_4_FINGER_SCANNER = "10 flat fingers with 4-finger scanner";

    // Beep Definitions
    protected final int __BEEP_FAIL__ = 0;
    protected final int __BEEP_SUCCESS__ = 1;
    protected final int __BEEP_OK__ = 2;
    protected final int __BEEP_DEVICE_COMMUNICATION_BREAK__ = 3;

    // LED color definitions
    protected final int __LED_COLOR_NONE__ = 0;
    protected final int __LED_COLOR_GREEN__ = 1;
    protected final int __LED_COLOR_RED__ = 2;
    protected final int __LED_COLOR_YELLOW__ = 3;

    //Key definitions
    protected final int __LEFT_KEY_BUTTON__ = 1;
    protected final int __RIGHT_KEY_BUTTON__ = 2;

    /* The number of finger segments set in the result image. */
    protected static final int FINGER_SEGMENT_COUNT = 4;

    /* *********************************************************************************************
     * PRIVATE CLASSES
     ******************************************************************************************** */

    /*
     * This class adjusts the data saved by the application for configuration changes.
     */
    protected class AppData {
        /* The currently selected USB device. */
        public int usbDevices = __INVALID_POS__;

        /* The currently selected capture sequence. */
        public int captureSeq = __INVALID_POS__;

        /* The current content of nfiq TextView. */
        public String nfiq = __NFIQ_DEFAULT__;

        /* The current content of the TextView time frame. */
        public String frameTime = __NA_DEFAULT__;

        /* The current image shown in the ImageView image preview. */
        public Bitmap imageBitmap = null;

        /* The current background colors of TextViews finger quality. */
        public int[] fingerQualityColors = new int[]
                {FINGER_QUALITY_NOT_PRESENT_COLOR, FINGER_QUALITY_NOT_PRESENT_COLOR,
                        FINGER_QUALITY_NOT_PRESENT_COLOR, FINGER_QUALITY_NOT_PRESENT_COLOR};

        /* Indicates whether you can click long on the preview of the ImageView image. */
        public boolean imagePreviewImageClickable = false;

        /* The current content of the TextView text overlay. */
        public String overlayText = "";

        /* The current content of the overlay color for OverlayText TextView. */
        public int overlayColor = PREVIEW_IMAGE_BACKGROUND;

        /* The current content of the TextView status message. */
        public String statusMessage = __NA_DEFAULT__;
    }

    protected class CaptureInfo {
        String PreCaptureMessage;        // to show in the fingerprint window
        String PostCaptuerMessage;        // to show in the fingerprint window
        ImageType ImageType;                // capture mode
        int NumberOfFinger;            // finger count number
        String fingerName;                // finger name (for example, left thumbs, left index ...)
    }

    ;

    /* *********************************************************************************************
     * PRIVATE FIELDS (COMPONENTS OF THE IU)
     ******************************************************************************************** */

    private TextView m_txtDeviceCount, m_txtNFIQ, m_txtFrameTime, m_txtStatusMessage, m_txtOverlayText,
            m_txtEnlargedScale, m_txtSDKVersion;
    private ImageView m_imgPreview, m_imgEnlargedView;
    private TextView[] m_txtFingerQuality = new TextView[FINGER_QUALITIES_COUNT];
    private Spinner m_cboUsbDevices, m_cboCaptureSeq;
    private Button m_btnCaptureStart, m_btnCaptureStop, m_btnCloseEnlargedDialog;
    private Dialog m_enlargedDialog;
    private Bitmap m_BitmapImage;

    /* *********************************************************************************************
     * PRIVATE FIELDS
     ******************************************************************************************** */

    /*
    An identifier for the unique instance of the IBScan class that will be the main interface}
     from the library, for operations such as getting the number of scanners (getDeviceCount ())
     and open scanners (openDeviceAsync ()).
     */
    private IBScan m_ibScan;

    /*
    An identifier for the open IBScanDevice (if any) that will be the interface
     to get data from the open scanner, including image capture (beginCaptureImage (),
     cancelCaptureImage ()) and the type of image that is captured.
     */
    private IBScanDevice m_ibScanDevice;

    /*
     *An object that will play a sound when the image capture is complete.
     */
    private PlaySound m_beeper = new PlaySound();

    /*
     * Information retained to show view.
     */
    private ImageData m_lastResultImage;
    private ImageData[] m_lastSegmentImages = new ImageData[FINGER_SEGMENT_COUNT];

    /*
     * Information retained for orientation changes.
     */
    private AppData m_savedData = new AppData();

    protected int m_nSelectedDevIndex = -1;                ///< Index of the selected device
    protected boolean m_bInitializing = false;                ///< Device initialization is in progress
    protected String m_ImgSaveFolderName = "";
    String m_ImgSaveFolder = "", m_ImgSubFolder = "";                    ///<Base folder for saving images     ///< Sub Folder for image sequence
    protected String m_strImageMessage = "";
    protected boolean m_bNeedClearPlaten = false, m_bBlank = false;
    protected boolean m_bSaveWarningOfClearPlaten;

    protected Vector<CaptureInfo> m_vecCaptureSeq = new Vector<CaptureInfo>();        ///< Sequence of capture steps
    protected int m_nCurrentCaptureStep = -1;                    ///< Current capture step

    protected IBScanDevice.LedState m_LedState;
    protected FingerQualityState[] m_FingerQuality = {FingerQualityState.FINGER_NOT_PRESENT, FingerQualityState.FINGER_NOT_PRESENT, FingerQualityState.FINGER_NOT_PRESENT, FingerQualityState.FINGER_NOT_PRESENT};
    protected ImageType m_ImageType;
    protected int m_nSegmentImageArrayCount = 0;
    protected SegmentPosition[] m_SegmentPositionArray;

    protected ArrayList<String> m_arrUsbDevices;
    protected ArrayList<String> m_arrCaptureSeq;

    protected byte[] m_drawBuffer;
    protected double m_scaleFactor;
    protected int m_leftMargin;
    protected int m_topMargin;


    // ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Global several definitions
    // ////////////////////////////////////////////////////////////////////////////////////////////////////


    /* *********************************************************************************************
     * HEREDITARY INTERFACE (CANCELLATION ACTIVITY)
     ******************************************************************************************** */

    /*
     * It is called when the activity starts.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_ibScan = IBScan.getInstance(this.getApplicationContext());
        m_ibScan.setScanListener(this);

        Resources r = Resources.getSystem();
        Configuration config = r.getConfiguration();

        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.ib_scan_activity);
        } else {
            setContentView(R.layout.ib_scan_activity_land);
        }

        /*Initialize UI Fields. */
        _InitUIFields();

        /*
         Make sure there are no connected USB devices that are IB scanners
         for those who have not been granted permission. For anyone who is,
         request permission; we should receive a callback when granted
         or deny permission and then when IBScan recognizes that the new devices
         are connected, which will result in another update.
         */

        final UsbManager manager = (UsbManager) this.getApplicationContext().getSystemService(Context.USB_SERVICE);
        final HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        final Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            final UsbDevice device = deviceIterator.next();
            final boolean isScanDevice = IBScan.isScanDevice(device);
            if (isScanDevice) {
                final boolean hasPermission = manager.hasPermission(device);
                if (!hasPermission) {
                    this.m_ibScan.requestPermission(device.getDeviceId());
                }
            }
        }

        OnMsg_UpdateDeviceList(false);

        /* Initialize the UI with data. */
        _PopulateUI();

        _TimerTaskThreadCallback thread = new _TimerTaskThreadCallback(__TIMER_STATUS_DELAY__);
        thread.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.ib_scan_activity);
        } else {
            setContentView(R.layout.ib_scan_activity_land);
        }

        /* Initialize UI fields for a new orientation. */
        _InitUIFields();

        OnMsg_UpdateDeviceList(true);

        /* Fill in the user interface with previous orientation data. */
        _PopulateUI();

    }

    /*
     * Free controller resources.
     */

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (int i = 0; i < 10; i++) {
            try {
                _ReleaseDevice();
                break;
            } catch (IBScanException ibse) {
                if (ibse.getType().equals(IBScanException.Type.RESOURCE_LOCKED)) {
                    Timber.d("Resource Locked");
                } else {
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        exitApp(this);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return null;
    }

    /* *********************************************************************************************
     * PRIVATE METHODS
     ******************************************************************************************** */

    /*
     *Initializes the user interface fields for a new orientation.
     */
    private void _InitUIFields() {
        m_txtDeviceCount =  findViewById(R.id.device_count);
        m_txtNFIQ =  findViewById(R.id.txtNFIQ);
        m_txtStatusMessage =  findViewById(R.id.txtStatusMessage);
        m_txtOverlayText =  findViewById(R.id.txtOverlayText);

        /* Hard-coded for four finger qualities. */

        m_txtFingerQuality[0] =  findViewById(R.id.scan_states_color1);
        m_txtFingerQuality[1] =  findViewById(R.id.scan_states_color2);
        m_txtFingerQuality[2] =  findViewById(R.id.scan_states_color3);
        m_txtFingerQuality[3] =  findViewById(R.id.scan_states_color4);

        m_txtFrameTime = findViewById(R.id.frame_time);

        m_txtSDKVersion = findViewById(R.id.version);

        m_imgPreview = findViewById(R.id.imgPreview);
        m_imgPreview.setOnLongClickListener(m_imgPreviewLongClickListener);
        m_imgPreview.setBackgroundColor(PREVIEW_IMAGE_BACKGROUND);

        m_btnCaptureStop = findViewById(R.id.stop_capture_btn);
        m_btnCaptureStop.setOnClickListener(this.m_btnCaptureStopClickListener);

        m_btnCaptureStart = findViewById(R.id.start_capture_btn);
        m_btnCaptureStart.setOnClickListener(this.m_btnCaptureStartClickListener);

        m_cboUsbDevices = findViewById(R.id.spinUsbDevices);
        m_cboCaptureSeq = findViewById(R.id.spinCaptureSeq);
    }

    /*
     * Fill in the user interface with previous orientation data.
     */
    private void _PopulateUI() {

        setSDKVersionInfo();

        if (m_savedData.usbDevices != __INVALID_POS__) {
            m_cboUsbDevices.setSelection(m_savedData.usbDevices);
        }

        if (m_savedData.captureSeq != __INVALID_POS__) {
            m_cboCaptureSeq.setSelection(m_savedData.captureSeq);
        }

        if (m_savedData.nfiq != null) {
            m_txtNFIQ.setText(m_savedData.nfiq);
        }

        if (m_savedData.frameTime != null) {
            m_txtFrameTime.setText(m_savedData.frameTime);
        }

        if (m_savedData.overlayText != null) {
            m_txtOverlayText.setTextColor(m_savedData.overlayColor);
            m_txtOverlayText.setText(m_savedData.overlayText);
        }

        if (m_savedData.imageBitmap != null) {
            m_imgPreview.setImageBitmap(m_savedData.imageBitmap);
        }

        for (int i = 0; i < FINGER_QUALITIES_COUNT; i++) {
            m_txtFingerQuality[i].setBackgroundColor(m_savedData.fingerQualityColors[i]);
        }

        if (m_BitmapImage != null) {
            m_BitmapImage.isRecycled();
        }

        m_imgPreview.setLongClickable(m_savedData.imagePreviewImageClickable);
    }

    // Get IBScan.
    protected IBScan getIBScan() {
        return (this.m_ibScan);
    }

    // Get opened or null IBScanDevice.
    protected IBScanDevice getIBScanDevice() {
        return (this.m_ibScanDevice);
    }

    // Set IBScanDevice.
    protected void setIBScanDevice(IBScanDevice ibScanDevice) {
        m_ibScanDevice = ibScanDevice;
        if (ibScanDevice != null) {
            ibScanDevice.setScanDeviceListener(this);
        }
    }

    /*
     * Set status message text box.
     */
    protected void _SetStatusBarMessage(final String s) {
        /* Make sure this happens in the UI thread.*/
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                m_txtStatusMessage.setText(s);
            }
        });
    }

    /*
     * Set image overlay message text box.
     */
    protected void _SetOverlayText(final String s, final int txtColor) {
        m_savedData.overlayText = s;
        m_savedData.overlayColor = txtColor;

        /* Make sure this happens in the UI thread. */
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_txtOverlayText.setTextColor(txtColor);
                m_txtOverlayText.setText(s);
            }
        });
    }

    /*
     * Timer task usando un Thread
     */
    class _TimerTaskThreadCallback extends Thread {
        private int timeInterval;

        _TimerTaskThreadCallback(int timeInterval) {
            this.timeInterval = timeInterval;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (getIBScanDevice() != null) {
                    OnMsg_DrawFingerQuality();

                    if (m_bNeedClearPlaten)
                        m_bBlank = !m_bBlank;
                }

                _Sleep(timeInterval);

                try {
                    Thread.sleep(timeInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Initialize the device using a thread
     */
    class _InitializeDeviceThreadCallback extends Thread {
        private int devIndex;

        _InitializeDeviceThreadCallback(int devIndex) {
            this.devIndex = devIndex;
        }

        @Override
        public void run() {
            try {
                m_bInitializing = true;
                IBScanDevice ibScanDeviceNew = getIBScan().openDevice(this.devIndex);
                setIBScanDevice(ibScanDeviceNew);
                m_bInitializing = false;

                if (ibScanDeviceNew != null) {
                    //getProperty device Width,Height
/*					String imageW = getIBScanDevice().getProperty(PropertyId.IMAGE_WIDTH);
					String imageH = getIBScanDevice().getProperty(PropertyId.IMAGE_HEIGHT);
					int	imageWidth = Integer.parseInt(imageW);
					int	imageHeight = Integer.parseInt(imageH);
//					m_BitmapImage = _CreateBitmap(imageWidth, imageHeight);
*/
                    int outWidth = m_imgPreview.getWidth() - 20;
                    int outHeight = m_imgPreview.getHeight() - 20;
                    m_BitmapImage = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);
                    m_drawBuffer = new byte[outWidth * outHeight * 4];

                    m_LedState = getIBScanDevice().getOperableLEDs();

                    OnMsg_CaptureSeqStart();
                }
            } catch (IBScanException ibse) {
                m_bInitializing = false;

                if (ibse.getType().equals(IBScanException.Type.DEVICE_ACTIVE)) {
                    _SetStatusBarMessage("[Error Code =-203] Device initialization failed because it is used by another thread / process.");
                } else if (ibse.getType().equals(IBScanException.Type.USB20_REQUIRED)) {
                    _SetStatusBarMessage("[Error Code =-209] Device initialization failed because SDK only works with USB 2.0.");
                } else {
                    _SetStatusBarMessage("Device initialization failed.");
                }

                OnMsg_UpdateDisplayResources();
            }
        }
    }

    protected Bitmap _CreateBitmap(int width, int height) {
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (bitmap != null) {
            final byte[] imageBuffer = new byte[width * height * 4];
            /*
             The image in the buffer is flipped vertically from
             from what the Bitmap class expects;
             we will invert it to compensate it while we move it to the buffer.
             */
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    imageBuffer[(y * width + x) * 4] =
                            imageBuffer[(y * width + x) * 4 + 1] =
                                    imageBuffer[(y * width + x) * 4 + 2] =
                                            (byte) 128;
                    imageBuffer[(y * width + x) * 4 + 3] = (byte) 255;
                }
            }
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(imageBuffer));
        }
        return (bitmap);
    }

    protected void _CalculateScaleFactors(ImageData image, int outWidth, int outHeight) {
        int left = 0, top = 0;
        int tmp_width = outWidth;
        int tmp_height = outHeight;
        int imgWidth = image.width;
        int imgHeight = image.height;
        int dispWidth, dispHeight, dispImgX, dispImgY;

        if (outWidth > imgWidth) {
            tmp_width = imgWidth;
            left = (outWidth - imgWidth) / 2;
        }
        if (outHeight > imgHeight) {
            tmp_height = imgHeight;
            top = (outHeight - imgHeight) / 2;
        }

        float ratio_width = (float) tmp_width / (float) imgWidth;
        float ratio_height = (float) tmp_height / (float) imgHeight;

        dispWidth = outWidth;
        dispHeight = outHeight;

        if (ratio_width >= ratio_height) {
            dispWidth = tmp_height * imgWidth / imgHeight;
            dispWidth -= (dispWidth % 4);
            dispHeight = tmp_height;
            dispImgX = (tmp_width - dispWidth) / 2 + left;
            dispImgY = top;
        } else {
            dispWidth = tmp_width;
            dispWidth -= (dispWidth % 4);
            dispHeight = tmp_width * imgHeight / imgWidth;
            dispImgX = left;
            dispImgY = (tmp_height - dispHeight) / 2 + top;
        }

        if (dispImgX < 0) {
            dispImgX = 0;
        }
        if (dispImgY < 0) {
            dispImgY = 0;
        }

        ///////////////////////////////////////////////////////////////////////////////////
        m_scaleFactor = (double) dispWidth / image.width;
        m_leftMargin = dispImgX;
        m_topMargin = dispImgY;
        ///////////////////////////////////////////////////////////////////////////////////
    }

    protected void _DrawOverlay_ImageText(Canvas canvas) {
/*
 * Draw text on bitmap image
 		Paint g = new Paint();
		g.setAntiAlias(true);
		if (m_bNeedClearPlaten)
			g.setColor(Color.RED);
		else
			g.setColor(Color.BLUE);
		g.setTypeface(Typeface.DEFAULT);
		g.setTextSize(20);
//		canvas.drawText(m_strImageMessage, 10, 20, g);
		canvas.drawText(m_strImageMessage, 20, 40, g);
*/

        /*
         * Draw textview over imageview
         */
        if (m_bNeedClearPlaten)
            _SetOverlayText(m_strImageMessage, Color.RED);
        else
            _SetOverlayText(m_strImageMessage, Color.BLUE);
    }

    protected void _DrawOverlay_WarningOfClearPlaten(Canvas canvas, int left, int top, int width, int height) {
        if (getIBScanDevice() == null)
            return;

        boolean idle = !m_bInitializing && (m_nCurrentCaptureStep == -1);

        if (!idle && m_bNeedClearPlaten && m_bBlank) {
            Paint g = new Paint();
            g.setStyle(Paint.Style.STROKE);
            g.setColor(Color.RED);
//			g.setStrokeWidth(10);
            g.setStrokeWidth(20);
            g.setAntiAlias(true);
            canvas.drawRect(left, top, width - 1, height - 1, g);
        }
    }

    protected void _DrawOverlay_ResultSegmentImage(Canvas canvas, ImageData image, int outWidth, int outHeight) {
        if (image.isFinal) {
//			if (m_chkDrawSegmentImage.isSelected())
            {
                // Draw a quadrangle for the segment image
                _CalculateScaleFactors(image, outWidth, outHeight);
                Paint g = new Paint();
                g.setColor(Color.rgb(0, 128, 0));
//				g.setStrokeWidth(1);
                g.setStrokeWidth(4);
                g.setAntiAlias(true);
                for (int i = 0; i < m_nSegmentImageArrayCount; i++) {
                    int x1, x2, x3, x4, y1, y2, y3, y4;
                    x1 = m_leftMargin + (int) (m_SegmentPositionArray[i].x1 * m_scaleFactor);
                    x2 = m_leftMargin + (int) (m_SegmentPositionArray[i].x2 * m_scaleFactor);
                    x3 = m_leftMargin + (int) (m_SegmentPositionArray[i].x3 * m_scaleFactor);
                    x4 = m_leftMargin + (int) (m_SegmentPositionArray[i].x4 * m_scaleFactor);
                    y1 = m_topMargin + (int) (m_SegmentPositionArray[i].y1 * m_scaleFactor);
                    y2 = m_topMargin + (int) (m_SegmentPositionArray[i].y2 * m_scaleFactor);
                    y3 = m_topMargin + (int) (m_SegmentPositionArray[i].y3 * m_scaleFactor);
                    y4 = m_topMargin + (int) (m_SegmentPositionArray[i].y4 * m_scaleFactor);

                    canvas.drawLine(x1, y1, x2, y2, g);
                    canvas.drawLine(x2, y2, x3, y3, g);
                    canvas.drawLine(x3, y3, x4, y4, g);
                    canvas.drawLine(x4, y4, x1, y1, g);
                }
            }
        }
    }

    protected void _DrawOverlay_RollGuideLine(Canvas canvas, ImageData image, int width, int height) {
        if (getIBScanDevice() == null || m_nCurrentCaptureStep == -1)
            return;

        if (m_ImageType == IBScanDevice.ImageType.ROLL_SINGLE_FINGER) {
            Paint g = new Paint();
            RollingData rollingdata;
            g.setAntiAlias(true);
            try {
                rollingdata = getIBScanDevice().getRollingInfo();

            } catch (IBScanException e) {
                rollingdata = null;
            }

            if ((rollingdata != null) && rollingdata.rollingLineX > 0 &&
                    (rollingdata.rollingState.equals(IBScanDevice.RollingState.TAKE_ACQUISITION) ||
                            rollingdata.rollingState.equals(IBScanDevice.RollingState.COMPLETE_ACQUISITION))) {
                _CalculateScaleFactors(image, width, height);
                int LineX = m_leftMargin + (int) (rollingdata.rollingLineX * m_scaleFactor);

                // Guide line for rolling
                if (rollingdata.rollingState.equals(IBScanDevice.RollingState.TAKE_ACQUISITION))
                    g.setColor(Color.RED);
                else if (rollingdata.rollingState.equals(IBScanDevice.RollingState.COMPLETE_ACQUISITION))
                    g.setColor(Color.GREEN);

                if (rollingdata.rollingLineX > -1) {
//					g.setStrokeWidth(2);
                    g.setStrokeWidth(4);
                    canvas.drawLine(LineX, 0, LineX, height, g);
                }
            }
        }
    }


    protected void _BeepFail() {
        try {
            IBScanDevice.BeeperType beeperType = getIBScanDevice().getOperableBeeper();
            if (beeperType != IBScanDevice.BeeperType.BEEPER_TYPE_NONE) {
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 12/*300ms = 12*25ms*/, 0, 0);
                _Sleep(150);
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 6/*150ms = 6*25ms*/, 0, 0);
                _Sleep(150);
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 6/*150ms = 6*25ms*/, 0, 0);
                _Sleep(150);
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 6/*150ms = 6*25ms*/, 0, 0);
            }
        } catch (IBScanException ibse) {
            /* devices for no beep chip send the tone to the
            "alarm" sequence (the classic beeps go there)
            with a volume of 30%
                    */
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300); // 300 is duration in ms
            _Sleep(300 + 150);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150); // 150 is duration in ms
            _Sleep(150 + 150);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150); // 150 is duration in ms
            _Sleep(150 + 150);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150); // 150 is duration in ms
        }
    }

    protected void _BeepSuccess() {
        try {
            IBScanDevice.BeeperType beeperType = getIBScanDevice().getOperableBeeper();
            if (beeperType != IBScanDevice.BeeperType.BEEPER_TYPE_NONE) {
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 4/*100ms = 4*25ms*/, 0, 0);
                _Sleep(50);
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 4/*100ms = 4*25ms*/, 0, 0);
            }
        } catch (IBScanException ibse) {
            /*
            devices for no beep chip
            send the tone to the "alarm" sequence (the classic beeps go there)
            with a volume of 30%
            */
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100 is duration in ms
            _Sleep(100 + 50);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100 is duration in ms
        }
    }

    protected void _BeepOk() {
        try {
            IBScanDevice.BeeperType beeperType = getIBScanDevice().getOperableBeeper();
            if (beeperType != IBScanDevice.BeeperType.BEEPER_TYPE_NONE) {
                getIBScanDevice().setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 4/*100ms = 4*25ms*/, 0, 0);
            }
        } catch (IBScanException ibse) {
            /*
            devices for no beep chip
            send the tone to the "alarm" sequence (the classic beeps go there)
            with a volume of 30%
            */
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100 is duration in ms
        }
    }

    protected void _BeepDeviceCommunicationBreak() {
        for (int i = 0; i < 8; i++) {
            /*
            send the tone to the "alarm" sequence (the classic beeps go there)
            with a volume of 30%
            */
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100 is duration in ms
            _Sleep(100 + 100);
        }
    }

    protected void _Sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }

    protected void _SetTxtNFIQScore(final String s) {
        this.m_savedData.nfiq = s;

        /* Make sure this happens in the UI thread. */
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_txtNFIQ.setText(s);
            }
        });
    }

    protected void _SetImageMessage(String s) {
        m_strImageMessage = s;
    }


    protected void _AddCaptureSeqVector(String PreCaptureMessage, String PostCaptuerMessage,
                                        IBScanDevice.ImageType imageType, int NumberOfFinger, String fingerName) {
        CaptureInfo info = new CaptureInfo();
        info.PreCaptureMessage = PreCaptureMessage;
        info.PostCaptuerMessage = PostCaptuerMessage;
        info.ImageType = imageType;
        info.NumberOfFinger = NumberOfFinger;
        info.fingerName = fingerName;
        m_vecCaptureSeq.addElement(info);
    }

    protected void _UpdateCaptureSequences() {
        try {
            // store the currently selected device
            String strSelectedText = "";
            int selectedSeq = m_cboCaptureSeq.getSelectedItemPosition();
            if (selectedSeq > -1)
                strSelectedText = m_cboCaptureSeq.getSelectedItem().toString();

            //populate the combobox
            m_arrCaptureSeq = new ArrayList<String>();

            m_arrCaptureSeq.add("- Please select -");
            final int devIndex = this.m_cboUsbDevices.getSelectedItemPosition() - 1;
            if (devIndex > -1) {
                IBScan.DeviceDesc devDesc = getIBScan().getDeviceDescription(devIndex);
                if ((devDesc.productName.equals("WATSON")) ||
                        (devDesc.productName.equals("WATSON MINI")) ||
                        (devDesc.productName.equals("SHERLOCK_ROIC")) ||
                        (devDesc.productName.equals("SHERLOCK"))) {
                    m_arrCaptureSeq.add(CAPTURE_SEQ_FLAT_SINGLE_FINGER);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_ROLL_SINGLE_FINGER);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_2_FLAT_FINGERS);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_10_SINGLE_ROLLED_FINGERS);
                } else if ((devDesc.productName.equals("COLUMBO")) ||
                        (devDesc.productName.equals("CURVE"))) {
                    m_arrCaptureSeq.add(CAPTURE_SEQ_FLAT_SINGLE_FINGER);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS);
                } else if ((devDesc.productName.equals("HOLMES")) ||
                        (devDesc.productName.equals("KOJAK")) ||
                        (devDesc.productName.equals("FIVE-0"))) {
                    m_arrCaptureSeq.add(CAPTURE_SEQ_FLAT_SINGLE_FINGER);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_ROLL_SINGLE_FINGER);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_2_FLAT_FINGERS);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_4_FLAT_FINGERS);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_10_SINGLE_ROLLED_FINGERS);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_10_FLAT_WITH_4_FINGER_SCANNER);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    R.layout.spinner_text_layout, m_arrCaptureSeq);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            m_cboCaptureSeq.setAdapter(adapter);
            m_cboCaptureSeq.setOnItemSelectedListener(m_captureTypeItemSelectedListener);


//			if (selectedSeq > -1)
//				this.m_cboCaptureSeq.setse(strSelectedText);

            OnMsg_UpdateDisplayResources();
        } catch (IBScanException e) {
            e.printStackTrace();
        }
    }

    protected void _ReleaseDevice() throws IBScanException {
        if (getIBScanDevice() != null) {
            if (getIBScanDevice().isOpened() == true) {
                getIBScanDevice().close();
                setIBScanDevice(null);
            }
        }

        m_nCurrentCaptureStep = -1;
        m_bInitializing = false;
    }

    protected void _SaveBitmapImage(ImageData image, String fingerName) {
/*		String filename = m_ImgSaveFolderName + ".bmp";

		try
	{
			image.saveToFile(filename, "BMP");
	}
		catch(IOException e)
	{
			e.printStackTrace();
	}
*/
    }

    protected void _SaveWsqImage(ImageData image, String fingerName) {
        String filename = m_ImgSaveFolderName + ".wsq";

        try {
            getIBScanDevice().wsqEncodeToFile(filename, image.buffer, image.width, image.height, image.pitch, image.bitsPerPixel, 500, 0.75, "");
        } catch (IBScanException e) {
            e.printStackTrace();
        }
    }

    protected void _SavePngImage(ImageData image, String fingerName) {
        String filename = m_ImgSaveFolderName + ".png";

        File file = new File(filename);
        FileOutputStream filestream = null;

        try {
            filestream = new FileOutputStream(file);
            final Bitmap bitmap = image.toBitmap();
            bitmap.compress(CompressFormat.PNG, 100, filestream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                filestream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void _SaveJP2Image(ImageData image, String fingerName) {
/*		String filename = m_ImgSaveFolderName + ".jp2";

		try
	{
			getIBScanDevice().SaveJP2Image(filename, image.buffer, image.width, image.height, image.pitch, image.resolutionX, image.resolutionY , 80);
	}
		catch (IBScanException e)
	{
			e.printStackTrace();
	}
		catch( StackOverflowError e)
	{
			System.out.println("Exception :"+ e);
			e.printStackTrace();
	}
*/
    }

    public void _SetLEDs(CaptureInfo info, int ledColor, boolean bBlink) {
        try {
            LedState ledState = getIBScanDevice().getOperableLEDs();
            if (ledState.ledCount == 0) {
                return;
            }
        } catch (IBScanException ibse) {
            ibse.printStackTrace();
        }

        int setLEDs = 0;

        if (ledColor == __LED_COLOR_NONE__) {
            try {
                getIBScanDevice().setLEDs(setLEDs);
            } catch (IBScanException ibse) {
                ibse.printStackTrace();
            }

            return;
        }

        if (m_LedState.ledType == IBScanDevice.LedType.FSCAN) {
            if (bBlink) {
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_BLINK_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_BLINK_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_BLINK_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_BLINK_RED;
                }
            }

            if (info.ImageType == IBScanDevice.ImageType.ROLL_SINGLE_FINGER) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_ROLL;
            }

            if ((info.fingerName.equals("SFF_Right_Thumb")) ||
                    (info.fingerName.equals("SRF_Right_Thumb"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_TWO_THUMB;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_RED;
                }
            } else if ((info.fingerName.equals("SFF_Left_Thumb")) ||
                    (info.fingerName.equals("SRF_Left_Thumb"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_TWO_THUMB;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_RED;
                }
            } else if ((info.fingerName.equals("TFF_2_Thumbs"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_TWO_THUMB;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_RED;
                }
            }
            ///////////////////LEFT HAND////////////////////
            else if ((info.fingerName.equals("SFF_Left_Index")) ||
                    (info.fingerName.equals("SRF_Left_Index"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_RED;
                }
            } else if ((info.fingerName.equals("SFF_Left_Middle")) ||
                    (info.fingerName.equals("SRF_Left_Middle"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_RED;
                }
            } else if ((info.fingerName.equals("SFF_Left_Ring")) ||
                    (info.fingerName.equals("SRF_Left_Ring"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_RED;
                }
            } else if ((info.fingerName.equals("SFF_Left_Little")) ||
                    (info.fingerName.equals("SRF_Left_Little"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_RED;
                }
            } else if ((info.fingerName.equals("4FF_Left_4_Fingers"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_RED;
                }
            }
            ///////////RIGHT HAND /////////////////////////
            else if ((info.fingerName.equals("SFF_Right_Index")) ||
                    (info.fingerName.equals("SRF_Right_Index"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_RED;
                }
            } else if ((info.fingerName.equals("SFF_Right_Middle")) ||
                    (info.fingerName.equals("SRF_Right_Middle"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_RED;
                }
            } else if ((info.fingerName.equals("SFF_Right_Ring")) ||
                    (info.fingerName.equals("SRF_Right_Ring"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_RED;
                }
            } else if ((info.fingerName.equals("SFF_Right_Little")) ||
                    (info.fingerName.equals("SRF_Right_Little"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_RED;
                }
            } else if ((info.fingerName.equals("4FF_Right_4_Fingers"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_RED;
                }
            }

            if (ledColor == __LED_COLOR_NONE__) {
                setLEDs = 0;
            }

            try {
                getIBScanDevice().setLEDs(setLEDs);
            } catch (IBScanException ibse) {
                ibse.printStackTrace();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Event-dispatch threads
    private void OnMsg_SetStatusBarMessage(final String s) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                _SetStatusBarMessage(s);
            }
        });
    }


    private void OnMsg_SetTxtNFIQScore(final String s) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                _SetTxtNFIQScore(s);
            }
        });
    }


    private void OnMsg_Beep(final int beepType) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (beepType == __BEEP_FAIL__)
                    _BeepFail();
                else if (beepType == __BEEP_SUCCESS__)
                    _BeepSuccess();
                else if (beepType == __BEEP_OK__)
                    _BeepOk();
                else if (beepType == __BEEP_DEVICE_COMMUNICATION_BREAK__)
                    _BeepDeviceCommunicationBreak();
            }
        });
    }

    private void OnMsg_CaptureSeqStart() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (getIBScanDevice() == null) {
                    OnMsg_UpdateDisplayResources();
                    return;
                }

                String strCaptureSeq = "";
                int nSelectedSeq = m_cboCaptureSeq.getSelectedItemPosition();
                if (nSelectedSeq > -1)
                    strCaptureSeq = m_cboCaptureSeq.getSelectedItem().toString();

                m_vecCaptureSeq.clear();

/** Please refer to the definition below.
 protected final String CAPTURE_SEQ_FLAT_SINGLE_FINGER 				= "Single flat finger";
 protected final String CAPTURE_SEQ_ROLL_SINGLE_FINGER 				= "Single rolled finger";
 protected final String CAPTURE_SEQ_2_FLAT_FINGERS 					= "2 flat fingers";
 protected final String CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS 			= "10 single flat fingers";
 protected final String CAPTURE_SEQ_10_SINGLE_ROLLED_FINGERS 		= "10 single rolled fingers";
 protected final String CAPTURE_SEQ_4_FLAT_FINGERS 					= "4 flat fingers";
 protected final String CAPTURE_SEQ_10_FLAT_WITH_4_FINGER_SCANNER 	= "10 flat fingers with 4-finger scanner";
 */
                if (strCaptureSeq.equals(CAPTURE_SEQ_FLAT_SINGLE_FINGER)) {
                    _AddCaptureSeqVector("Please put a finger on the sensor!",
                            "Keep your finger on the sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Unknown");
                }


                if (strCaptureSeq.equals(CAPTURE_SEQ_ROLL_SINGLE_FINGER)) {
                    _AddCaptureSeqVector("Please put a finger on the sensor!",
                            "Rotate finger!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SRF_Unknown");
                }

                if (strCaptureSeq == CAPTURE_SEQ_2_FLAT_FINGERS) {
                    _AddCaptureSeqVector("Please put two fingers on the sensor!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_TWO_FINGERS,
                            2,
                            "TFF_Unknown");
                }

                if (strCaptureSeq == CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS) {
                    _AddCaptureSeqVector("Please put your right thumb on the sensor!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Right_Thumb");

                    _AddCaptureSeqVector("Please put the right index on the sensor!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Right_Index");

                    _AddCaptureSeqVector("Please put the right middle on the sensor!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Right_Middle");

                    _AddCaptureSeqVector("Please put the right ring on the sensor!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Right_Ring");

                    _AddCaptureSeqVector("Please put little right on the sensor!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Right_Little");

                    _AddCaptureSeqVector("Please put your left thumb on the sensor!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Left_Thumb");

                    _AddCaptureSeqVector("Please put the left index on the sensor!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Left_Index");

                    _AddCaptureSeqVector("Please put the left middle in the sensor!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Left_Middle");

                    _AddCaptureSeqVector("Please put the left ring on the sensor!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Left_Ring");

                    _AddCaptureSeqVector("Please put little left on the sensor!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER,
                            1,
                            "SFF_Left_Little");
                }

                if (strCaptureSeq == CAPTURE_SEQ_10_SINGLE_ROLLED_FINGERS) {
                    _AddCaptureSeqVector("Please put your right thumb on the sensor!",
                            "Rotate finger!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Right_Thumb");

                    _AddCaptureSeqVector("Please put the right index on the sensor!",
                            "Rotate finger!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Right_Index");

                    _AddCaptureSeqVector("Please put the right middle on the sensor!",
                            "Rotate finger!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Right_Middle");

                    _AddCaptureSeqVector("Please put the right ring on the sensor!",
                            "Rotate finger!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Right_Ring");

                    _AddCaptureSeqVector("Please put little right on the sensor!",
                            "Rotate finger!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Right_Little");

                    _AddCaptureSeqVector("Please put your left thumb on the sensor!",
                            "Rotate finger!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Left_Thumb");

                    _AddCaptureSeqVector("Please put the left index on the sensor!",
                            "Rotate finger!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Left_Index");

                    _AddCaptureSeqVector("Please put the left middle in the sensor!",
                            "Rotate finger!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Left_Middle");

                    _AddCaptureSeqVector("Please put the left ring on the sensor!",
                            "Rotate finger!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Left_Ring");

                    _AddCaptureSeqVector("Please put little left on the sensor!",
                            "Rotate finger!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER,
                            1,
                            "SFF_Left_Little");
                }

                if (strCaptureSeq == CAPTURE_SEQ_4_FLAT_FINGERS) {
                    _AddCaptureSeqVector("Please put 4 fingers on the sensor!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_FOUR_FINGERS,
                            4,
                            "4FF_Unknown");
                }

                if (strCaptureSeq == CAPTURE_SEQ_10_FLAT_WITH_4_FINGER_SCANNER) {
                    _AddCaptureSeqVector("Please put 4 fingers on the sensor right!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_FOUR_FINGERS,
                            4,
                            "4FF_Right_4_Fingers");

                    _AddCaptureSeqVector("Please place the 4 left fingers on the sensor!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_FOUR_FINGERS,
                            4,
                            "4FF_Left_4_Fingers");

                    _AddCaptureSeqVector("Please put 2-thumbs on the sensor!",
                            "Keep your fingers on the sensor!",
                            IBScanDevice.ImageType.FLAT_TWO_FINGERS,
                            2,
                            "TFF_2_Thumbs");
                }

                OnMsg_CaptureSeqNext();
            }
        });
    }

    private void OnMsg_CaptureSeqNext() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (getIBScanDevice() == null)
                    return;

                m_bBlank = false;
                for (int i = 0; i < 4; i++)
                    m_FingerQuality[i] = FingerQualityState.FINGER_NOT_PRESENT;

                m_nCurrentCaptureStep++;
                if (m_nCurrentCaptureStep >= m_vecCaptureSeq.size()) {
                    // All of capture sequence completely
                    CaptureInfo tmpInfo = new CaptureInfo();
                    _SetLEDs(tmpInfo, __LED_COLOR_NONE__, false);
                    m_nCurrentCaptureStep = -1;

                    OnMsg_UpdateDisplayResources();
                    return;
                }

                try {
/*					if (m_chkDetectSmear.isSelected())
					{
						getIBScanDevice().setProperty(IBScanDevice.PropertyId.ROLL_MODE, "1");
						String strValue = String.valueOf(m_cboSmearLevel.getSelectedIndex());
						getIBScanDevice().setProperty(IBScanDevice.PropertyId.ROLL_LEVEL, strValue);
			}
			else
			{
						getIBScanDevice().setProperty(IBScanDevice.PropertyId.ROLL_MODE, "0");
					}
*/
                    /*
                    Perform a capture delay to display the image
                    of the result in the multiple capture mode (500 ms)
                    */
                    if (m_nCurrentCaptureStep > 0) {
                        _Sleep(500);
                        m_strImageMessage = "";
                    }

                    CaptureInfo info = m_vecCaptureSeq.elementAt(m_nCurrentCaptureStep);

                    IBScanDevice.ImageResolution imgRes = IBScanDevice.ImageResolution.RESOLUTION_500;
                    boolean bAvailable = getIBScanDevice().isCaptureAvailable(info.ImageType, imgRes);
                    if (!bAvailable) {
                        _SetStatusBarMessage("Capture mode (" + info.ImageType + ") not available");
                        m_nCurrentCaptureStep = -1;
                        OnMsg_UpdateDisplayResources();
                        return;
                    }

                    // Start capture
                    int captureOptions = 0;
//					if (m_chkAutoContrast.isSelected())
                    captureOptions |= IBScanDevice.OPTION_AUTO_CONTRAST;
//					if (m_chkAutoCapture.isSelected())
                    captureOptions |= IBScanDevice.OPTION_AUTO_CAPTURE;
//					if (m_chkIgnoreFingerCount.isSelected())
                    captureOptions |= IBScanDevice.OPTION_IGNORE_FINGER_COUNT;

                    getIBScanDevice().beginCaptureImage(info.ImageType, imgRes, captureOptions);

                    String strMessage = info.PreCaptureMessage;
                    _SetStatusBarMessage(strMessage);
//					if (!m_chkAutoCapture.isSelected())
//						strMessage += "\r\nPress button 'Take Result Image' when image is good!";

                    _SetImageMessage(strMessage);
                    m_strImageMessage = strMessage;

                    m_ImageType = info.ImageType;

                    _SetLEDs(info, __LED_COLOR_RED__, true);

                    OnMsg_UpdateDisplayResources();
                } catch (IBScanException ibse) {
                    ibse.printStackTrace();
                    _SetStatusBarMessage("Could not execute beginCaptureImage()");
                    m_nCurrentCaptureStep = -1;
                }
            }
        });
    }

    private void OnMsg_cboUsbDevice_Changed() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (m_nSelectedDevIndex == m_cboUsbDevices.getSelectedItemPosition())
                    return;

                m_nSelectedDevIndex = m_cboUsbDevices.getSelectedItemPosition();
                if (getIBScanDevice() != null) {
                    try {
                        _ReleaseDevice();
                    } catch (IBScanException ibse) {
                        ibse.printStackTrace();
                    }
                }

                _UpdateCaptureSequences();
            }
        });
    }

    private void OnMsg_UpdateDeviceList(final boolean bConfigurationChanged) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    boolean idle = (!m_bInitializing && (m_nCurrentCaptureStep == -1)) ||
                            (bConfigurationChanged);

                    if (idle) {
                        m_btnCaptureStop.setEnabled(false);
                        m_btnCaptureStart.setEnabled(false);
                    }

                    //store the currently selected device
                    String strSelectedText = "";
                    int selectedDev = m_cboUsbDevices.getSelectedItemPosition();
                    if (selectedDev > -1)
                        strSelectedText = m_cboUsbDevices.getSelectedItem().toString();

                    m_arrUsbDevices = new ArrayList<String>();

                    m_arrUsbDevices.add("- Please select -");
                    // populate combo box
                    int devices = getIBScan().getDeviceCount();
                    setDeviceCount(devices);
//					m_cboUsbDevices.setMaximumRowCount(devices + 1);

                    selectedDev = 0;
                    for (int i = 0; i < devices; i++) {
                        IBScan.DeviceDesc devDesc = getIBScan().getDeviceDescription(i);
                        String strDevice;
                        strDevice = devDesc.productName + "_v" + devDesc.fwVersion + "(" + devDesc.serialNumber + ")";

                        m_arrUsbDevices.add(strDevice);
                        if (strDevice == strSelectedText)
                            selectedDev = i + 1;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(IbScanActivity.this,
                            R.layout.spinner_text_layout, m_arrUsbDevices);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    m_cboUsbDevices.setAdapter(adapter);
                    m_cboUsbDevices.setOnItemSelectedListener(m_cboUsbDevicesItemSelectedListener);

                    if ((selectedDev == 0 && (m_cboUsbDevices.getCount() == 2)))
                        selectedDev = 1;

                    m_cboUsbDevices.setSelection(selectedDev);

                    if (idle) {
                        OnMsg_cboUsbDevice_Changed();
                        _UpdateCaptureSequences();
                    }
                } catch (IBScanException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void OnMsg_UpdateDisplayResources() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                boolean selectedDev = m_cboUsbDevices.getSelectedItemPosition() > 0;
                boolean captureSeq = m_cboCaptureSeq.getSelectedItemPosition() > 0;
                boolean idle = !m_bInitializing && (m_nCurrentCaptureStep == -1);
                boolean active = !m_bInitializing && (m_nCurrentCaptureStep != -1);
                boolean uninitializedDev = selectedDev && (getIBScanDevice() == null);

                m_cboUsbDevices.setEnabled(idle);
                m_cboCaptureSeq.setEnabled(selectedDev && idle);

                m_btnCaptureStart.setEnabled(captureSeq);
                m_btnCaptureStop.setEnabled(active);

//				m_chkAutoContrast.setEnabled(selectedDev && idle );
//				m_chkAutoCapture.setEnabled(selectedDev && idle );
//				m_chkIgnoreFingerCount.setEnabled(selectedDev && idle );
//				m_chkSaveImages.setEnabled(selectedDev && idle );
//				m_btnImageFolder.setEnabled(selectedDev && idle );

//				m_chkUseClearPlaten.setEnabled(uninitializedDev);

                if (active) {
                    m_btnCaptureStart.setText("Take result image");
                } else {
                    m_btnCaptureStart.setText("Start");
                }
            }
        });
    }

    private void OnMsg_AskRecapture(final IBScanException imageStatus) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String askMsg;

                askMsg = "[Warning = " + imageStatus.getType().toString() + "] You want a recapture?";

                AlertDialog.Builder dlgAskRecapture = new AlertDialog.Builder(IbScanActivity.this);
                dlgAskRecapture.setMessage(askMsg);
                dlgAskRecapture.setPositiveButton("Si",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Para recuperar la posición actual de los dedos
                                m_nCurrentCaptureStep--;
                                OnMsg_CaptureSeqNext();
                            }
                        });
                dlgAskRecapture.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                OnMsg_CaptureSeqNext();
                            }
                        });

                dlgAskRecapture.show();
            }
        });
    }


    private void OnMsg_DeviceCommunicationBreak() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (getIBScanDevice() == null)
                    return;

                _SetStatusBarMessage("Device communication broke");

                try {
                    _ReleaseDevice();

                    OnMsg_Beep(__BEEP_DEVICE_COMMUNICATION_BREAK__);
                    OnMsg_UpdateDeviceList(false);
                } catch (IBScanException ibse) {
                    if (ibse.getType().equals(IBScanException.Type.RESOURCE_LOCKED)) {
                        OnMsg_DeviceCommunicationBreak();
                    }
                }
            }
        });
    }

    private void OnMsg_DrawImage(final IBScanDevice device, final ImageData image) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int destWidth = m_imgPreview.getWidth() - 20;
                int destHeight = m_imgPreview.getHeight() - 20;
//				int outImageSize = destWidth * destHeight;

                try {
                    if (destHeight <= 0 || destWidth <= 0)
                        return;

                    if (destWidth != m_BitmapImage.getWidth() || destHeight != m_BitmapImage.getHeight()) {
                        /*
                         if the image size is changed (e.g., modified capture type
                         from flat finger to rolled finger) Create bitmap again
                         */

                        m_BitmapImage = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888);
                        m_drawBuffer = new byte[destWidth * destHeight * 4];
                    }

                    if (image.isFinal) {
                        getIBScanDevice().generateDisplayImage(image.buffer, image.width, image.height,
                                m_drawBuffer, destWidth, destHeight, (byte) 255, 2 /*IBSU_IMG_FORMAT_RGB32*/, 2 /*HIGH QUALITY*/, true);
/*						for (int i=0; i<destWidth*destHeight; i++)
					{
							if (m_drawBuffer[i] != -1) {
								OnMsg_Beep(__BEEP_OK__);
						break;
					}
						}
*/
                    } else {
                        getIBScanDevice().generateDisplayImage(image.buffer, image.width, image.height,
                                m_drawBuffer, destWidth, destHeight, (byte) 255, 2 /*IBSU_IMG_FORMAT_RGB32*/, 0 /*LOW QUALITY*/, true);
                    }
                } catch (IBScanException e) {
                    e.printStackTrace();
                }

                m_BitmapImage.copyPixelsFromBuffer(ByteBuffer.wrap(m_drawBuffer));
                // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                Canvas canvas = new Canvas(m_BitmapImage);

                _DrawOverlay_ImageText(canvas);
                _DrawOverlay_WarningOfClearPlaten(canvas, 0, 0, destWidth, destHeight);
                _DrawOverlay_ResultSegmentImage(canvas, image, destWidth, destHeight);
                _DrawOverlay_RollGuideLine(canvas, image, destWidth, destHeight);
/*				_DrawOverlay_WarningOfClearPlaten(canvas, 0, 0, image.width, image.height);
				_DrawOverlay_ResultSegmentImage(canvas, image, image.width, image.height);
				_DrawOverlay_RollGuideLine(canvas, image, image.width, image.height);
			 */
                m_savedData.imageBitmap = m_BitmapImage;
                m_imgPreview.setImageBitmap(m_BitmapImage);
            }
        });
    }

    private void OnMsg_DrawFingerQuality() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // Update the value in the fingerQuality field and the flash button.
                for (int i = 0; i < 4; i++) {
                    int color;
                    if (m_FingerQuality[i] == IBScanDevice.FingerQualityState.GOOD)
                        color = Color.rgb(0, 128, 0);
                    else if (m_FingerQuality[i] == IBScanDevice.FingerQualityState.FAIR)
                        color = Color.rgb(255, 128, 0);
                    else if (m_FingerQuality[i] == IBScanDevice.FingerQualityState.POOR ||
                            m_FingerQuality[i] == IBScanDevice.FingerQualityState.INVALID_AREA_TOP ||
                            m_FingerQuality[i] == IBScanDevice.FingerQualityState.INVALID_AREA_BOTTOM ||
                            m_FingerQuality[i] == IBScanDevice.FingerQualityState.INVALID_AREA_LEFT ||
                            m_FingerQuality[i] == IBScanDevice.FingerQualityState.INVALID_AREA_RIGHT
                            )
                        color = Color.rgb(255, 0, 0);
                    else
                        color = Color.LTGRAY;

                    m_savedData.fingerQualityColors[i] = color;
                    m_txtFingerQuality[i].setBackgroundColor(color);
                }
            }
        });
    }

    /*
     *Show Toast message in the UI thread.
     */
    private void showToastOnUiThread(final String message, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), message, duration);
                toast.show();
            }
        });
    }

    /*
     * Set SDK version in SDK version text field.
     */
    private void setSDKVersionInfo() {
        String txtValue;

        try {
            SdkVersion sdkVersion;

            sdkVersion = m_ibScan.getSdkVersion();
            txtValue = "SDK version: " + sdkVersion.file;
        } catch (IBScanException e) {
            txtValue = "failed: "+ e;
        }

        /* Make sure this occurs on the UI thread. */
        final String txtValueTemp = txtValue;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_txtSDKVersion.setText(txtValueTemp);
            }
        });
    }

    /*
     * Set device count in device count text box.
     */
    private void setDeviceCount(final int deviceCount) {
        /* Make sure this occurs on the UI thread. */
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_txtDeviceCount.setText("" + deviceCount);
            }
        });
    }

    /*
     * Set frame time in frame time field.  Save value for orientation change.
     */
    private void setFrameTime(final String s) {
        m_savedData.frameTime = s;

        /* Make sure this occurs on the UI thread. */
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_txtFrameTime.setText(s);
            }
        });
    }

    /*
     * Show enlarged image in the popup window.
     */
    private void showEnlargedImage() {
        /*
         * Sanity check.  Make sure the image exists.
         */
        if (this.m_lastResultImage == null) {
            showToastOnUiThread("No information from the last image", Toast.LENGTH_SHORT);
            return;
        }

        m_enlargedDialog = new Dialog(this, R.style.Enlarged);
        m_enlargedDialog.setContentView(R.layout.enlarged);
        m_enlargedDialog.setCancelable(false);

        final Bitmap bitmap = m_lastResultImage.toBitmap();
        m_imgEnlargedView =  m_enlargedDialog.findViewById(R.id.enlarged_image);
        m_btnCloseEnlargedDialog =  m_enlargedDialog.findViewById(R.id.btnClose);
        m_txtEnlargedScale = m_enlargedDialog.findViewById(R.id.txtDisplayImgScale);

        m_imgEnlargedView.setScaleType(ImageView.ScaleType.CENTER);
        m_imgEnlargedView.setImageBitmap(bitmap);
        m_btnCloseEnlargedDialog.setOnClickListener(m_btnCloseEnlargedDialogClickListener);

        final PhotoViewAttacher mAttacher = new PhotoViewAttacher(m_imgEnlargedView);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int disp_w = size.x - 20; //m_imgEnlargedView.getWidth();
        int disp_h = size.y - 50; //m_imgEnlargedView.getHeight();
        float ratio_w = (float) disp_w / m_lastResultImage.width;
        float ratio_h = (float) disp_h / m_lastResultImage.height;
        float ratio_1x = 0.0f;
        if (ratio_w > ratio_h) {
            ratio_1x = (float) m_lastResultImage.height / disp_h;
        } else {
            ratio_1x = (float) m_lastResultImage.width / disp_w;
        }

        mAttacher.setMaximumScale(ratio_1x * 8);
        mAttacher.setMediumScale(ratio_1x * 4);
        mAttacher.setMinimumScale(ratio_1x);

        final float zoom_1x = ratio_1x;
        mAttacher.setOnMatrixChangeListener(new PhotoViewAttacher.OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rectF) {
                m_txtEnlargedScale.setText(String.format("Scale : %1$.1f x", mAttacher.getScale() / zoom_1x));
            }
        });


        m_imgEnlargedView.post(new Runnable() {
            @Override
            public void run() {
                mAttacher.setScale(zoom_1x, false);
            }
        });

        this.m_enlargedDialog.show();
    }

    /*
     *Compress the image and attach it to an email
     * using an installed email client.
     */
    private void sendImageInEmail(final ImageData imageData, final String fileName) {

        Toast.makeText(this, "consume soap sendImageInEmail", Toast.LENGTH_SHORT).show();

        boolean created = false;
        ArrayList ur = new ArrayList();
        try {
            {
                File fp = new File(Environment.getExternalStorageDirectory().getPath() + "/" + fileName);
                fp.createNewFile();

                final FileOutputStream fstream = new FileOutputStream(fp);
                final Bitmap bitmap = imageData.toBitmap();
                bitmap.compress(CompressFormat.PNG, 100, fstream);
                fstream.close();

                ur.add(Uri.fromFile(fp));        // Result image
            }

            for (int i = 0; i < m_nSegmentImageArrayCount; i++) {
                File fp = new File(Environment.getExternalStorageDirectory().getPath() + "/segment_" + i + "_" + fileName);
                try {
                    fp.createNewFile();

                    final FileOutputStream fstream = new FileOutputStream(fp);
                    final Bitmap bitmap = m_lastSegmentImages[i].toBitmap();
                    bitmap.compress(CompressFormat.PNG, 100, fstream);
                    fstream.close();

                    ur.add(Uri.fromFile(fp));
                } catch (IOException ioe) {
                    Toast.makeText(getApplicationContext(), "Could not create image for email", Toast.LENGTH_LONG).show();
                }
            }

            created = true;
        } catch (IOException ioe) {
            Toast.makeText(getApplicationContext(), "Could not create image for email", Toast.LENGTH_LONG).show();
        }


        /* If the file was created, send the email. */
        if (created) {
            attachAndSendEmail(ur, "Fingerprint Image", fileName);
        }
    }

    /*
     * Attach the file to an email and send it.
     */
    private void attachAndSendEmail(final ArrayList ur, final String subject, final String message) {
        final Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
//		i.putExtra(Intent.EXTRA_STREAM,  ur);
        i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ur);
        i.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(i, "Sending email..."));
        } catch (ActivityNotFoundException anfe) {
            showToastOnUiThread("No email clients installed", Toast.LENGTH_LONG);
        }
    }

    /*
     * Request to send email with image.
     */
    private void promptForEmail(final ImageData imageData) {
        /* The dialog should be displayed from the UI thread.*/

        Toast.makeText(this, "consume soap promptForEmail", Toast.LENGTH_SHORT).show();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final LayoutInflater inflater = getLayoutInflater();
                final View fileNameView = inflater.inflate(R.layout.file_name_dialog, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(IbScanActivity.this)
                        .setView(fileNameView)
                        .setTitle("Enter the file name")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                final EditText text = (EditText) fileNameView.findViewById(R.id.file_name);
                                final String fileName = text.getText().toString();

                                /* E-mail image in background thread. */
                                Thread threadEmail = new Thread() {
                                    @Override
                                    public void run() {
                                        sendImageInEmail(imageData, fileName);
                                    }
                                };
                                threadEmail.start();
                            }
                        })
                        .setNegativeButton("Cancel", null);
                EditText text = (EditText) fileNameView.findViewById(R.id.file_name);
                text.setText(FILE_NAME_DEFAULT + "." + "png");

                builder.create().show();
            }
        });
    }

    /*
     * Exit application.
     */
    private static void exitApp(Activity ac) {
        ac.moveTaskToBack(true);
        ac.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }



    /* *********************************************************************************************
     * EVENTS HANDLERS
     ******************************************************************************************** */

    /*
     * Handle click on the "Start capture" button".
     */
    private OnClickListener m_btnCaptureStartClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (m_bInitializing)
                return;

            int devIndex = m_cboUsbDevices.getSelectedItemPosition() - 1;
            if (devIndex < 0)
                return;

            if (m_nCurrentCaptureStep != -1) {
                try {
                    boolean IsActive = getIBScanDevice().isCaptureActive();
                    if (IsActive) {
                        // Capture the image manually for the active device
                        getIBScanDevice().captureImageManually();
                        return;
                    }
                } catch (IBScanException ibse) {
                    _SetStatusBarMessage("IBScanDevice.takeResultImageManually() returned exception "
                            + ibse.getType().toString() + ".");
                }
            }

            if (getIBScanDevice() == null) {
                m_bInitializing = true;
                _InitializeDeviceThreadCallback thread = new _InitializeDeviceThreadCallback(m_nSelectedDevIndex - 1);
                thread.start();
            } else {
                OnMsg_CaptureSeqStart();
            }

            OnMsg_UpdateDisplayResources();
        }
    };

    /*
     * Handle click on the "Stop capture" button".
     */
    private OnClickListener m_btnCaptureStopClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (getIBScanDevice() == null)
                return;

            // Cancel image capture for the active device
            try {
                // Cancel image capture for the active device.
                getIBScanDevice().cancelCaptureImage();
                CaptureInfo tmpInfo = new CaptureInfo();
                _SetLEDs(tmpInfo, __LED_COLOR_NONE__, false);
                m_nCurrentCaptureStep = -1;
                m_bNeedClearPlaten = false;
                m_bBlank = false;

                _SetStatusBarMessage("Sequence of aborted capture");
                m_strImageMessage = "";
                _SetImageMessage("");
                OnMsg_UpdateDisplayResources();
            } catch (IBScanException ibse) {
                _SetStatusBarMessage("cancel returned exception " + ibse.getType().toString() + ".");
            }
        }
    };

    /*
     * Handle long clicks on the image view.
     */
    private OnLongClickListener m_imgPreviewLongClickListener = new OnLongClickListener() {
        /*
         * When the image view is long-clicked, show a popup menu.
         */
        @Override
        public boolean onLongClick(final View v) {
            final PopupMenu popup = new PopupMenu(IbScanActivity.this, IbScanActivity.this.m_txtNFIQ);
            popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                /*
                 * Handle click on a menu item.
                 */
                @Override
                public boolean onMenuItemClick(final MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.email_image:
                            promptForEmail(m_lastResultImage);
                            return (true);
                        case R.id.enlarge:
                            showEnlargedImage();
                            return (true);
                        default:
                            return (false);
                    }
                }

            });

            final MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.scanimage_menu, popup.getMenu());
            popup.show();

            return (true);
        }
    };

    /*
     * Handle click on the spinner that determines the USB devices.
     */
    private OnItemSelectedListener m_cboUsbDevicesItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> parent, final View view, final int pos,
                                   final long id) {
            OnMsg_cboUsbDevice_Changed();
            m_savedData.usbDevices = pos;
        }

        @Override
        public void onNothingSelected(final AdapterView<?> parent) {
            m_savedData.usbDevices = __INVALID_POS__;
        }
    };

    /*
     * Handle click on the spinner that determines the fingerprint capture.
     */
    private OnItemSelectedListener m_captureTypeItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> parent, final View view, final int pos,
                                   final long id) {
            if (pos == 0) {
                m_btnCaptureStart.setEnabled(false);
            } else {
                m_btnCaptureStart.setEnabled(true);
            }

            m_savedData.captureSeq = pos;
        }

        @Override
        public void onNothingSelected(final AdapterView<?> parent) {
            m_savedData.captureSeq = __INVALID_POS__;
        }
    };

    /*
     * Hide the extended dialog box, if it exists.
     */
    private OnClickListener m_btnCloseEnlargedDialogClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (m_enlargedDialog != null) {
                m_enlargedDialog.cancel();
                m_enlargedDialog = null;
            }
        }
    };

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC INTERFACE: IBScanListener METHODS
    // //////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void scanDeviceAttached(final int deviceId) {
        showToastOnUiThread("Device " + deviceId + " attached", Toast.LENGTH_SHORT);

        /*
         Check if we have permission to access this device.
         Request permission to appear as an IB scanner.
         */
        final boolean hasPermission = m_ibScan.hasPermission(deviceId);
        if (!hasPermission) {
            m_ibScan.requestPermission(deviceId);
        }
    }

    @Override
    public void scanDeviceDetached(final int deviceId) {
        /*
         A device has been disconnected. We should also receive
         a callback scanDeviceCountChanged (), where we can update the screen.
         If our device was disconnected during scanning,
         we should also receive a callback from DeviceCommunicationBreak ().
         */
        showToastOnUiThread("Device " + deviceId + " separated", Toast.LENGTH_SHORT);
    }

    @Override
    public void scanDevicePermissionGranted(final int deviceId, final boolean granted) {
        if (granted) {
            /*
            This device should appear as an IB scanner. We can wait for him
            Callback ScanDeviceCountChanged () to refresh the screen.
             */
            showToastOnUiThread("Permission granted to the device " + deviceId, Toast.LENGTH_SHORT);
        } else {
            showToastOnUiThread("Permission denied to device " + deviceId, Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void scanDeviceCountChanged(final int deviceCount) {
        OnMsg_UpdateDeviceList(false);
    }

    @Override
    public void scanDeviceInitProgress(final int deviceIndex, final int progressValue) {
        OnMsg_SetStatusBarMessage("Initializing the device..." + progressValue + "%");
    }

    @Override
    public void scanDeviceOpenComplete(final int deviceIndex, final IBScanDevice device,
                                       final IBScanException exception) {
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC INTERFACE: IBScanDeviceListener METHODS
    // //////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void deviceCommunicationBroken(final IBScanDevice device) {
        OnMsg_DeviceCommunicationBreak();
    }

    @Override
    public void deviceImagePreviewAvailable(final IBScanDevice device, final ImageData image) {
        setFrameTime(String.format("%1$.3f ms", image.frameTime * 1000));
        OnMsg_DrawImage(device, image);
    }

    @Override
    public void deviceFingerCountChanged(final IBScanDevice device, final FingerCountState fingerState) {
        if (m_nCurrentCaptureStep >= 0) {
            CaptureInfo info = m_vecCaptureSeq.elementAt(m_nCurrentCaptureStep);
            if (fingerState == IBScanDevice.FingerCountState.NON_FINGER) {
                _SetLEDs(info, __LED_COLOR_RED__, true);
            } else {
                _SetLEDs(info, __LED_COLOR_YELLOW__, true);
            }
        }
    }

    @Override
    public void deviceFingerQualityChanged(final IBScanDevice device, final FingerQualityState[] fingerQualities) {
        for (int i = 0; i < fingerQualities.length; i++) {
            m_FingerQuality[i] = fingerQualities[i];
        }

        OnMsg_DrawFingerQuality();
    }

    @Override
    public void deviceAcquisitionBegun(final IBScanDevice device, final ImageType imageType) {
        if (imageType.equals(IBScanDevice.ImageType.ROLL_SINGLE_FINGER)) {
            OnMsg_Beep(__BEEP_OK__);
            m_strImageMessage = "When finished, remove your finger from the sensor";
            _SetImageMessage(m_strImageMessage);
            _SetStatusBarMessage(m_strImageMessage);
        }
    }

    @Override
    public void deviceAcquisitionCompleted(final IBScanDevice device, final ImageType imageType) {
        if (imageType.equals(IBScanDevice.ImageType.ROLL_SINGLE_FINGER)) {
            OnMsg_Beep(__BEEP_OK__);
        } else {
            OnMsg_Beep(__BEEP_SUCCESS__);
            _SetImageMessage("Remove the fingers from the sensor");
            _SetStatusBarMessage("Acquisition completed, post processing..");
        }
    }

    @Override
    public void deviceImageResultAvailable(final IBScanDevice device, final ImageData image,
                                           final ImageType imageType, final ImageData[] splitImageArray) {
        /* TODO: ALTERNATIVELY, USE RESULTS IN THIS FUNCTION */
    }

    @Override
    public void deviceImageResultExtendedAvailable(IBScanDevice device, IBScanException imageStatus,
                                                   final ImageData image, final ImageType imageType, final int detectedFingerCount,
                                                   final ImageData[] segmentImageArray, final SegmentPosition[] segmentPositionArray) {
        setFrameTime(String.format("%1$.3f ms", image.frameTime * 1000));

        m_savedData.imagePreviewImageClickable = true;
        m_imgPreview.setLongClickable(true);
        m_lastResultImage = image;
        m_lastSegmentImages = segmentImageArray;

        // The value of imageStatus is greater than "STATUS_OK", successful image acquisition.
        if (imageStatus == null /*STATUS_OK*/ ||
                imageStatus.getType().compareTo(IBScanException.Type.INVALID_PARAM_VALUE) > 0) {
            if (imageType.equals(IBScanDevice.ImageType.ROLL_SINGLE_FINGER)) {
                OnMsg_Beep(__BEEP_SUCCESS__);
            }
        }

        if (m_bNeedClearPlaten) {
            m_bNeedClearPlaten = false;
            OnMsg_DrawFingerQuality();
        }

        // The value of imageStatus is greater than "STATUS_OK", successful image acquisition.
        if (imageStatus == null /*STATUS_OK*/ ||
                imageStatus.getType().compareTo(IBScanException.Type.INVALID_PARAM_VALUE) > 0) {
            // Acquisition of successful image
            CaptureInfo info = m_vecCaptureSeq.elementAt(m_nCurrentCaptureStep);
            _SetLEDs(info, __LED_COLOR_GREEN__, false);

            // save Picture
/*			if (m_chkSaveImages.isSelected())
			{
				// Show chooser for output image.
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(imageFilter);
				int returnVal = chooser.showSaveDialog(IBScanUltimate_Sample.this);

				if (returnVal == JFileChooser.APPROVE_OPTION)
				{						
					_SetStatusBarMessage("Saving image...");
					m_ImgSaveFolderName = chooser.getCurrentDirectory().toString() + File.separator + chooser.getSelectedFile().getName();
					_SaveBitmapImage(image, info.fingerName);
					_SaveWsqImage(image, info.fingerName);
					_SavePngImage(image, info.fingerName);
					_SaveJP2Image(image, info.fingerName);

					//save segmented fingers
					for (int i = 0; i < detectedFingerCount; i++)
				{
						String segmentName = info.fingerName + "_Segment_" + String.valueOf(i);
						_SaveBitmapImage(segmentImageArray[i], segmentName);
						_SaveWsqImage(segmentImageArray[i], segmentName);
						_SavePngImage(segmentImageArray[i], segmentName);
						_SaveJP2Image(segmentImageArray[i], segmentName);
				}
			}
			}
*/
//			if (m_chkDrawSegmentImage.isSelected())
            {
                m_nSegmentImageArrayCount = detectedFingerCount;
                m_SegmentPositionArray = segmentPositionArray;
            }

            // NFIQ
//			if (m_chkNFIQScore.isSelected())
            {
                byte[] nfiq_score = {0, 0, 0, 0};
                try {
                    for (int i = 0, segment_pos = 0; i < 4; i++) {
                        if (m_FingerQuality[i].ordinal() != IBScanDevice.FingerQualityState.FINGER_NOT_PRESENT.ordinal()) {
                            nfiq_score[i] = (byte) getIBScanDevice().calculateNfiqScore(segmentImageArray[segment_pos++]);
                        }
                    }
                } catch (IBScanException ibse) {
                    ibse.printStackTrace();
                }

                OnMsg_SetTxtNFIQScore("" + nfiq_score[0] + "-" + nfiq_score[1] + "-" + nfiq_score[2] + "-" + nfiq_score[3]);
            }

            if (imageStatus == null /*STATUS_OK*/) {
                m_strImageMessage = "Acquisition completed successfully";
                _SetImageMessage(m_strImageMessage);
                _SetStatusBarMessage(m_strImageMessage);
            } else {
                // > IBSU_STATUS_OK
                m_strImageMessage = "Acquisition of warning (warning code = " + imageStatus.getType().toString() + ")";
                _SetImageMessage(m_strImageMessage);
                _SetStatusBarMessage(m_strImageMessage);

                OnMsg_DrawImage(device, image);
                OnMsg_AskRecapture(imageStatus);
                return;
            }
        } else {
            // < IBSU_STATUS_OK
            m_strImageMessage = "Acquisition failed (Error code = " + imageStatus.getType().toString() + ")";
            _SetImageMessage(m_strImageMessage);
            _SetStatusBarMessage(m_strImageMessage);

            // Stop all acquisition
            m_nCurrentCaptureStep = (int) m_vecCaptureSeq.size();
        }

        OnMsg_DrawImage(device, image);

        OnMsg_CaptureSeqNext();
    }

    @Override
    public void devicePlatenStateChanged(final IBScanDevice device, final PlatenState platenState) {
        if (platenState.equals(IBScanDevice.PlatenState.HAS_FINGERS))
            m_bNeedClearPlaten = true;
        else
            m_bNeedClearPlaten = false;

        if (platenState.equals(IBScanDevice.PlatenState.HAS_FINGERS)) {
            m_strImageMessage = "Please remove your fingers on the deck first!";
            _SetImageMessage(m_strImageMessage);
            _SetStatusBarMessage(m_strImageMessage);
        } else {
            if (m_nCurrentCaptureStep >= 0) {
                CaptureInfo info = m_vecCaptureSeq.elementAt(m_nCurrentCaptureStep);

                //Show message for image acquisition again
                String strMessage = info.PreCaptureMessage;

                _SetStatusBarMessage(strMessage);
//				if (!m_chkAutoCapture.isSelected())
//					strMessage += "\r\nPress button 'Take Result Image' when image is good!";

                _SetImageMessage(strMessage);
                m_strImageMessage = strMessage;
            }
        }

        OnMsg_DrawFingerQuality();
    }

    @Override
    public void deviceWarningReceived(final IBScanDevice device, final IBScanException warning) {
        _SetStatusBarMessage("Warning Received " + warning.getType().toString());
    }

    @Override
    public void devicePressedKeyButtons(IBScanDevice device, int pressedKeyButtons) {
        _SetStatusBarMessage("PressedKeyButtons " + pressedKeyButtons);

        boolean selectedDev = m_cboUsbDevices.getSelectedItemPosition() > 0;
        boolean idle = m_bInitializing && (m_nCurrentCaptureStep == -1);
        boolean active = m_bInitializing && (m_nCurrentCaptureStep != -1);
        try {
            if (pressedKeyButtons == __LEFT_KEY_BUTTON__) {
                if (selectedDev && idle) {
                    System.out.println("Start capture");
                    device.setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 4/*100ms = 4*25ms*/, 0, 0);
                    this.m_btnCaptureStart.performClick();
                }
            } else if (pressedKeyButtons == __RIGHT_KEY_BUTTON__) {
                if ((active)) {
                    System.out.println("Stop capture");
                    device.setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2/*Sol*/, 4/*100ms = 4*25ms*/, 0, 0);
                    this.m_btnCaptureStop.performClick();
                }
            }
        } catch (IBScanException e) {
            e.printStackTrace();
        }
    }
}
