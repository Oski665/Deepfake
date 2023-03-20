package com.example.deepfake;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.widget.ImageView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivityTest {

    @Mock
    Looper mainLooper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(Looper.getMainLooper()).thenReturn(mainLooper);
    }


    @Test
    public void testOpenCameraButton() {

        MainActivity activity = new MainActivity();

        Looper.prepareMainLooper();

        FloatingActionButton openCameraButton = activity.findViewById(R.id.fbtOpenCamera);

        openCameraButton.performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertNotNull(startedIntent);
        assertEquals(MainActivity.class.getName(), startedIntent.getComponent().getClassName());
    }

    @Test
    public void testOpenGalleryButton() {
        MainActivity activity = new MainActivity();

        Looper.prepareMainLooper();

        FloatingActionButton openGalleryButton = activity.findViewById(R.id.fbtOpenGallery);

        openGalleryButton.performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertNotNull(startedIntent);
        assertEquals(MainActivity.class.getName(), startedIntent.getComponent().getClassName());
    }

    @Test
    public void testSaveAndLoadImage() throws Exception {
        Bitmap image = BitmapFactory.decodeResource(getInstrumentation().getContext().getResources(), R.drawable.test_image);
        File imageFile = saveImageToInternalStorage(image, "test_image.jpg");

        Bitmap loadedImage = loadImageFromInternalStorage(imageFile);

        assertBitmapEquals(image, loadedImage);
    }

    private File saveImageToInternalStorage(Bitmap image, String fileName) throws IOException {
        File imageFile = new File(getInstrumentation().getContext().getFilesDir(), fileName);
        FileOutputStream fos = new FileOutputStream(imageFile);

        image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();

        return imageFile;
    }

    private Bitmap loadImageFromInternalStorage(File imageFile) throws IOException {
        FileInputStream fis = new FileInputStream(imageFile);
        Bitmap image = BitmapFactory.decodeStream(fis);
        fis.close();

        return image;
    }

    private void assertBitmapEquals(Bitmap expected, Bitmap actual) {
        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getHeight(), actual.getHeight());

        for (int x = 0; x < expected.getWidth(); x++) {
            for (int y = 0; y < expected.getHeight(); y++) {
                assertEquals(expected.getPixel(x, y), actual.getPixel(x, y));
            }
        }
    }

    @Test
    public void testSwitchViewButton() {

        MainActivity activity = new MainActivity();

        Looper.prepareMainLooper();

        FloatingActionButton switchViewButton = (FloatingActionButton) activity.findViewById(R.id.fbtSwitchView);
        ImageView imageView = (ImageView) activity.findViewById(R.id.ivCamera);

        Drawable firstImage = getInstrumentation().getTargetContext().getResources().getDrawable(R.drawable.first_image);
        Drawable secondImage = getInstrumentation().getTargetContext().getResources().getDrawable(R.drawable.second_image);

        switchViewButton.performClick();
        assertEquals(firstImage, imageView.getDrawable());

        switchViewButton.performClick();
        assertEquals(secondImage, imageView.getDrawable());
    }

}
