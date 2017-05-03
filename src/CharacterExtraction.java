import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tanmay on 22/3/17.
 */
public class CharacterExtraction {
    private static Mat number_plates = new Mat();
    CharacterExtraction(Mat a) throws Exception {
        number_plates = a;
        start();
    }
    private void start () throws Exception {
        Frame1.jLabel2.setText("Searching the Characters");
        Mat sob =new Mat();
        Mat img;
        img=number_plates;
        int area = img.width()*img.height();
        //LPE.imshow("new", img.clone());
        Imgproc.cvtColor(img,sob, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(sob, sob, 0, 255, Imgproc.THRESH_OTSU+Imgproc.THRESH_BINARY);
        //LPE.imshow("Thre", sob.clone());

        List<Mat> characters= new ArrayList<Mat>();
        List<Integer> center= new ArrayList<Integer>();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(sob, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint2f approxCurve = new MatOfPoint2f();

        int numchar=0;
        Rect[] boundingRect = new Rect[contours.size()];
        //For each contour found
        if(contours.size()>4)
            for (int i = 0; i < contours.size(); i++)
            {
                //Convert contours(i) from MatOfPoint to MatOfPoint2f
                MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
                //Processing on mMOP2f1 which is in type MatOfPoint2f
                double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
                Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

                //Convert back to MatOfPoint
                MatOfPoint points = new MatOfPoint(approxCurve.toArray());

                // Get bounding rect of contour
                boundingRect[i] = Imgproc.boundingRect(points);
                if ( boundingRect[i].height* (double)boundingRect[i].width < area/10 && boundingRect[i].height* (double)boundingRect[i].width > area/45 && boundingRect[i].height >img.height()/3 && boundingRect[i].width <img.width()/5 && boundingRect[i].height/(double)boundingRect[i].width > 1)
                {
                    center.add(2*boundingRect[i].x + boundingRect[i].width);
                    characters.add(new Mat(img,boundingRect[i]));
                    //Imgproc.rectangle(img, new Point(boundingRect[i].x, boundingRect[i].y), new Point(boundingRect[i].x + boundingRect[i].width, boundingRect[i].y + boundingRect[i].height), new Scalar(0, 0, 255));

                }
            }
        Integer[] cent=new Integer[center.size()];
        center.toArray(cent);
        for(int i=0;i<characters.size();i++) {
            int index = 0;
            int largest = Integer.MIN_VALUE;
            for ( int j = 0; j < cent.length; j++ )
            {
                if ( cent[j] > largest )
                {
                    largest = cent[j];
                    index = j;
                }
            }
            cent[index]=Integer.MIN_VALUE;
            //Processing for classification
            Mat ch = new Mat();
            ch = characters.get(index);
            Imgproc.resize(ch, ch, new Size(128, 128));
            Imgproc.cvtColor(ch, ch, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(ch, ch, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY_INV);
            Imgcodecs.imwrite("/home/tanmay/SE Project/Custom_Dataset/Example/" + Integer.toString(++numchar) + ".jpg", ch);

        }
        LicensePlateExtraction.imshow("License Plate", img.clone());
        Frame1.jLabel2.setText("Applying OCR");
        org.deeplearning4j.examples.dataExamples.OCRImagePipelineLoad recognise=new org.deeplearning4j.examples.dataExamples.OCRImagePipelineLoad();
        Frame1.jLabel2.setText("Completed!");


    }
}
