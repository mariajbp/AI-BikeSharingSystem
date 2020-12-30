package Util;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ui.ApplicationFrame;

import java.awt.*;
import java.util.ArrayList;

public class ChartWindow extends ApplicationFrame {



        public ChartWindow( String[] chartTitles, JFreeChart[] charts) {
            super( "Charts" );
            ArrayList<ChartPanel> panels= new ArrayList<>();

            for(int i=0;i<chartTitles.length;i++){
                String tit = chartTitles[i];
                JFreeChart chart = charts[i];
            ChartPanel chartPanel = new ChartPanel( chart );
            chartPanel.setPreferredSize(new java.awt.Dimension( 560 ,i==0? 360 :340) );
            panels.add(chartPanel);
            }

            getContentPane().add(panels.get(0), BorderLayout.SOUTH);

           // getContentPane().add(panels.get(1), BorderLayout.SOUTH);

            getContentPane().add(panels.get(2), BorderLayout.NORTH);


        }

    public void plot() {
        this.pack();
        this.setVisible( true );
    }




}
