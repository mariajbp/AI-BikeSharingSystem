package Agents;


import Util.ChartWindow;
import Util.ConfigVars;
import Util.Mapa;
import Util.Posicao;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AgenteInterface extends Agent {
    private Mapa mapa;
    private AID monitor;
    private HashMap<AID,Integer> stationOccupation = new HashMap<>();
    private HashMap<AID, Integer[]> stationPricing = new HashMap<>();
    private ChartWindow chartWindow;
    private int tickNo;
    private DefaultCategoryDataset datasetOccup;
    private DefaultCategoryDataset datasetPricing;
    private XYSeries xySeries;


    @Override
    protected void setup() {
        super.setup();
        tickNo=0;
        mapa = new Mapa(100);

        DFAgentDescription df = new DFAgentDescription();
        ServiceDescription s = new ServiceDescription();
        s.setType("monitor");
        df.addServices(s);
        try {
             monitor = DFService.search(this, df)[0].getName();
        }catch (Exception e){e.printStackTrace();}

        ////Stations
        ACLMessage getStas = new ACLMessage(ACLMessage.REQUEST);
        getStas.addReceiver(monitor);
        try {
            getStas.setContentObject(new Object[]{"Stations"});
        } catch (Exception e) {e.printStackTrace();}
        send(getStas);


        ACLMessage recStas = receive();
        while((recStas=receive())==null){}
        try {mapa.setStations((Map<AID, Posicao>) recStas.getContentObject());} catch (UnreadableException e){e.printStackTrace();}
        System.out.println(mapa);




        System.out.println(mapa);
        addBehaviour(new InterfaceBehavior(this));
        addBehaviour(new InterfRecBehavior());
    }


    public class InterfaceBehavior extends TickerBehaviour {

        public InterfaceBehavior(Agent a) {
            super(a, (long)(((float)2000) * ConfigVars.SPEED));
        }

        @Override
        protected void onTick() {
            tickNo++;
            ACLMessage reqAllStats = new ACLMessage(ACLMessage.REQUEST);
            try {
                reqAllStats.setContentObject(new Object[]{"Stats"});
            } catch (IOException e) {
                e.printStackTrace();
            }
            for(AID st : mapa.getStationSet()){reqAllStats.addReceiver(st);}
            ////Users
            reqAllStats.addReceiver(monitor);
            send(reqAllStats);

            drawChartOccup();


        }



    }

    private void drawChartOccup() {

       if(datasetOccup == null){ datasetOccup =new DefaultCategoryDataset( );}
       if(datasetPricing == null){ datasetPricing =new DefaultCategoryDataset( );}
        XYSeriesCollection datasetNumUsers= null;
       if(xySeries == null){ datasetNumUsers =new XYSeriesCollection(); datasetNumUsers.addSeries(xySeries = new XYSeries("Numero de Utilizadores"));}

       xySeries.add( tickNo,mapa.getNumUsers());
       stationOccupation.forEach((sta, occ)->{
            datasetOccup.setValue( occ , sta.getLocalName(),"Taxa de Ocupação" );
        });
       stationPricing.forEach((sta, pArray)->{
           datasetPricing.setValue( pArray[0] , "Desconto",sta.getLocalName() );
           datasetPricing.setValue( pArray[1] ,"Normal", sta.getLocalName());
           datasetPricing.setValue( pArray[2] ,"Inflacionado", sta.getLocalName() );
        });


       if(chartWindow == null){
/////////// STATION OCCUPATION
        JFreeChart barChart = ChartFactory.createBarChart(
                "Taxa de ocupação das estaçoes",
                " ",
                "%",
                datasetOccup,
                PlotOrientation.VERTICAL,
                true, true, false);
        CategoryPlot plot = (CategoryPlot) barChart.getPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0, 100);
////////////// NUM USERS
        JFreeChart userChart = ChartFactory.createXYLineChart(
                "Numero de Utilizadores no Sistema",
                "Tick",
                "#Users",
                    datasetNumUsers,
                    PlotOrientation.VERTICAL,
                    false,true,false);
 /*           XYPlot plot2 = (XYPlot) userChart.getPlot();
            NumberAxis rangeAxis2 = (NumberAxis) plot2.getRangeAxis();
            rangeAxis2.setLowerBound(0);

  */
///////////// PRICING
           JFreeChart priceChart = ChartFactory.createBarChart(
                   "Distribuição de preços por estação",
                   "Categoria",
                   "#Transaçoes",
                   datasetPricing,
                   PlotOrientation.VERTICAL,
                   true, true, false);
           CategoryPlot plot3 = (CategoryPlot) priceChart.getPlot();
           NumberAxis rangeAxis3 = (NumberAxis) plot3.getRangeAxis();

          // rangeAxis3.setLowerBound(0);

       chartWindow = new ChartWindow(new String[]{"Taxa de ocupação das estaçoes",
                                                "Numero de Utilizadores no Sistema",
                                                "Distribuição de preços por estação"},
                                   new JFreeChart[]{barChart,
                                                    userChart,
                                                    priceChart});
       chartWindow.plot();
        }
    }

    private class InterfRecBehavior extends CyclicBehaviour {


        @Override
        public void action() {
            ACLMessage msg = receive();
            if(msg!=null){
                AID sender = msg.getSender();
                switch (msg.getPerformative()){
                    case ACLMessage.INFORM:{
                        try {
                            Object[] cnt =(Object[]) msg.getContentObject();

                        switch ((String)cnt[0]){
                            case "Station":
                                stationOccupation.put(sender,(int)cnt[1]);
                                stationPricing.put(sender,(Integer[]) cnt[2] );

                                break;
                            case "Users" :
                                mapa.setUsers((HashMap<AID, Posicao>) cnt[1]);
                                break;
                        }
                        } catch (UnreadableException e) {e.printStackTrace();}
                        break;
                    }
                }
            }
        }
    }
}
