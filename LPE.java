import org.opencv.core.Core;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.MatOfPoint;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
/**
 * Created by tanmay on 29/1/17.
 */
public class LPE{
    public static void imshow(String text, Mat img) {

        Imgproc.resize(img, img, new Size(640, 480));
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", img, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setTitle(text);
            frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
            frame.pack();
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        System.out.println("Searching for plates");
        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            Mat img_org = Imgcodecs.imread("/home/tanmay/SE Project/snapshots/test_017.jpg", 1);
            imshow("Real", img_org);
            Mat img = new Mat();
            Imgproc.cvtColor(img_org, img, Imgproc.COLOR_BGR2GRAY);
            Mat sob1 = new Mat();
            Mat sob2 = new Mat();
            Mat can = new Mat();
            Mat comb = new Mat();
            Imgproc.Sobel(img, sob1, -1, 1, 0, 3, 1, 0, Imgproc.CHAIN_APPROX_SIMPLE);
            Imgproc.Sobel(img, sob2, -1, 0, 1, 3, 1, 0, Imgproc.CHAIN_APPROX_SIMPLE);
            //   imshow("Sob1",sob1);
            // imshow("Sob2",sob2);
            Core.add(sob1, sob2, comb);
            //imshow("Comb1", comb);
            Imgproc.threshold(comb, comb, 125, 255, 0);
            //imshow("Comb2", comb);
            Imgproc.Canny(img, can, 120, 205, 3, false);
            imshow("cam", can);


            Imgproc.GaussianBlur(can, can, new Size(5, 5), 5);

            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(can, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

            MatOfPoint2f approxCurve = new MatOfPoint2f();


            Rect[] boundingRect = new Rect[contours.size()];
            int[] count = new int[contours.size()];
            //For each contour found
            for (int i = 0; i < contours.size(); i++) {
                //Convert contours(i) from MatOfPoint to MatOfPoint2f
                MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
                //Processing on mMOP2f1 which is in type MatOfPoint2f
                double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
                Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

                //Convert back to MatOfPoint
                MatOfPoint points = new MatOfPoint(approxCurve.toArray());

                // Get bounding rect of contour
                Rect rectangle = Imgproc.boundingRect(points);
                boundingRect[i] = Imgproc.boundingRect(points);
                for (int j = 0; j < i; j++) {
                    if (boundingRect[j].x <= rectangle.x && boundingRect[j].y <= rectangle.y && boundingRect[j].x + boundingRect[j].width >= rectangle.x + rectangle.width && boundingRect[j].y + boundingRect[j].height >= rectangle.y + rectangle.height)
                        count[j]++;
                    else if (boundingRect[j].x >= rectangle.x && boundingRect[j].y >= rectangle.y && boundingRect[j].x + boundingRect[j].width <= rectangle.x + rectangle.width && boundingRect[j].y + boundingRect[j].height <= rectangle.y + rectangle.height)
                        count[i]++;
                }

            }


            for (int i = 0; i < contours.size(); i++) {
                if (boundingRect[i].width / boundingRect[i].height > 2.5 && boundingRect[i].width / boundingRect[i].height < 5.5 && boundingRect[i].width * boundingRect[i].height < 15000 && count[i] > 4)
                 {

                    System.out.println(i+"-->"+(double) boundingRect[i].width / boundingRect[i].height);
                        Imgproc.putText(img_org,Integer.toString(i),new Point(boundingRect[i].x, boundingRect[i].y),2,1,new Scalar(i*25, i*25, i*25));
                    //    Imgproc.putText(img_org,Double.toString((double) boundingRect.width*boundingRect.height),new Point(boundingRect.x, boundingRect.y+boundingRect.height),2,1,new Scalar(i*25, i*25, i*25));
                    Imgproc.rectangle(img_org, new Point(boundingRect[i].x, boundingRect[i].y), new Point(boundingRect[i].x + boundingRect[i].width, boundingRect[i].y + boundingRect[i].height), new Scalar(0, 0, 255));
                }
            }


            imshow("new", img_org);


        }

    }


