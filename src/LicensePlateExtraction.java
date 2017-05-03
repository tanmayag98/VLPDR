import org.opencv.core.Core;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.MatOfPoint;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
/**
 * Created by tanmay on 29/1/17.
 */


public class LicensePlateExtraction {
    public static  boolean flag=false;
    public static java.awt.Point cord;
    public LicensePlateExtraction()
    {
        try {
//            run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void imshow(String text, Mat img) {

//      Imgproc.resize(img, img, new Size(640, 480));
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", img, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
            JFrame frame = new JFrame();
            frame.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    cord=mouseEvent.getPoint();
                    flag=true;
                }

                @Override
                public void mousePressed(MouseEvent mouseEvent) {


                }

                @Override
                public void mouseReleased(MouseEvent mouseEvent) {


                }

                @Override
                public void mouseEntered(MouseEvent mouseEvent) {

                }

                @Override
                public void mouseExited(MouseEvent mouseEvent) {

                }
            });
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setTitle(text);
            frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
            frame.pack();
            frame.setVisible(true);
            while(!flag){
                System.out.print(" ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception
    {

        Frame1 frame = new Frame1();
        frame.setVisible(true);
        while (Frame1.path==null){
            System.out.println("No file");
        }
        Mat passing_image = null;
        frame.jLabel2.setText("Searching for plates");
        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //Read the Image
        {   Mat img_org = Imgcodecs.imread(Frame1.path, 1);
            System.out.println("image read");
            //Show the read image
            //imshow("Real", img_org);
            //Declaring mats for processing
            Mat gray = new Mat();
            Mat can = new Mat();
            Mat img_clone = new Mat();
            img_clone = img_org.clone();
            boolean debug=false;
            //Processing
            Imgproc.cvtColor(img_org, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.Canny(gray, can, 120, 205, 3, false);
            System.out.println("image read 2");

            if(debug)
            {   imshow("Edges", can);

            }

            Imgproc.GaussianBlur(can, can, new Size(3, 3), 5);
            //For storing multiple number plates (if there)
            List<Rect> number_plates= new ArrayList<Rect>();
            //Find Contours
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(can, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
            MatOfPoint2f approxCurve = new MatOfPoint2f();

            Rect[] boundingRect = new Rect[contours.size()];
            int[] count = new int[contours.size()];
            //For each contour found
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
                Rect rectangle = Imgproc.boundingRect(points);
                boundingRect[i] = Imgproc.boundingRect(points);
                //Calcuate the number of rectangles in each rectangle for filtering later
                for (int j = 0; j < i; j++)
                {
                    if (boundingRect[j].x <= rectangle.x && boundingRect[j].y <= rectangle.y && boundingRect[j].x + boundingRect[j].width >= rectangle.x + rectangle.width && boundingRect[j].y + boundingRect[j].height >= rectangle.y + rectangle.height)
                        count[j]++;
                    else if (boundingRect[j].x >= rectangle.x && boundingRect[j].y >= rectangle.y && boundingRect[j].x + boundingRect[j].width <= rectangle.x + rectangle.width && boundingRect[j].y + boundingRect[j].height <= rectangle.y + rectangle.height)
                        count[i]++;
                }
            }

            //Filter for license plate and Draw Rectangle for every rectangle found in the image
            for (int i = 0; i < contours.size(); i++)
            {   //Filtering conditions
                if (boundingRect[i].width / boundingRect[i].height > 2.5 && boundingRect[i].width / boundingRect[i].height < 5.5 && boundingRect[i].width * boundingRect[i].height < 150000 && count[i] > 5)
                {
                    if(debug)
                    {   System.out.println(i + "-->" + (double) boundingRect[i].width / boundingRect[i].height);
                        Imgproc.putText(img_clone,Integer.toString(i),new Point(boundingRect[i].x, boundingRect[i].y),2,1,new Scalar(i*25, i*25, i*25));
                        Imgproc.putText(img_clone,Double.toString((double) boundingRect[i].width*boundingRect[i].height),new Point(boundingRect[i].x, boundingRect[i].y+boundingRect[i].height),2,1,new Scalar(i*25, i*25, i*25));
                    }
                    number_plates.add(boundingRect[i]);
                    Imgproc.rectangle(img_clone, new Point(boundingRect[i].x, boundingRect[i].y), new Point(boundingRect[i].x + boundingRect[i].width, boundingRect[i].y + boundingRect[i].height), new Scalar(0, 0, 255));

                }
            }

            imshow("Input Image", img_clone.clone());
            for(int i=0;i<number_plates.size();i++)
            {
                Point p1 =new Point(number_plates.get(i).x, number_plates.get(i).y);
                Point p2 = new Point(number_plates.get(i).x + number_plates.get(i).width, number_plates.get(i).y + number_plates.get(i).height);
                if (cord.getX() > p1.x && cord.getX()< p2.x  && cord.getY()> p1.y && cord.getY()< p2.y) {
                    passing_image = new Mat(img_org, number_plates.get(i));
                    break;
                }

            }
            //imshow("Img_org", passing_image);

            //Extract the characters from the required image
            CharacterExtraction chars = new CharacterExtraction(passing_image);
        }
        frame.jTextField1.setText(org.deeplearning4j.examples.dataExamples.OCRImagePipelineLoad.reg_id);
    }
}