package com.neurotechx.smartphonebci;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.google.common.base.Optional;
import com.google.common.math.DoubleMath;
import com.neurotechx.smartphonebci.driver.dsp.BinnedValues;

import java.text.DecimalFormat;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link SpectrumPlot#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SpectrumPlot extends Fragment
{

    private XYPlot plot;
    private Spectrum spectrum = new Spectrum("Spectrum");
    private double max = Double.MIN_VALUE;

    public SpectrumPlot()
    {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SpectrumPlot.
     */
    // TODO: Rename and change types and number of parameters
    public static SpectrumPlot newInstance()
    {
        SpectrumPlot fragment = new SpectrumPlot();
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        MainActivity.plot = Optional.of(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_spectrum_plot, container, false);

        // initialize our XYPlot reference:
        plot = (XYPlot) view.findViewById(R.id.plot);


        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.GREEN, null, null, null);

        //series1//Format.setPointLabelFormatter(new PointLabelFormatter());

        // series1Format.setInterpolationParams(
        //        new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));


        // add a new series' to the xyplot:
        plot.addSeries(spectrum, series1Format);


        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);

        // rotate domain labels 45 degrees to make them more compact horizontally:
        plot.getGraphWidget().setDomainLabelOrientation(-45);
        plot.getGraphWidget().setPaddingBottom(50);
        plot.getGraphWidget().setPaddingLeft(120);
        plot.getGraphWidget().setPaddingTop(20);
        plot.getGraphWidget().setPaddingRight(20);

        //   plot.setRangeBoundaries(0,1,BoundaryMode.FIXED);

        //setting up the value formats
        plot.setRangeValueFormat(new DecimalFormat("0.00"));
        plot.setDomainValueFormat(new DecimalFormat("0.00"));
        // Inflate the layout for this fragment
        return view;
    }

    public void push(BinnedValues values)
    {
        spectrum.setValues(values);
        plot.redraw();
    }


    class Spectrum implements XYSeries
    {
        private Optional<BinnedValues> values = Optional.absent();

        private String title;

        private double epochMax = Double.MIN_VALUE;

        public Spectrum(String title)
        {
            this.title = title;
        }


        public void setValues(BinnedValues binnedValues)
        {
            values = Optional.of(binnedValues);
        }

        @Override
        public String getTitle()
        {
            return title;
        }

        @Override
        public int size()
        {

            if (values.isPresent())
            {
                //len minus 10Hz
                int tenHz = (int) (10 / values.get().getResolution());
                double[] vals = Arrays.copyOfRange(
                        values.get().getValues(),
                        (int) (4 / values.get().getResolution()),
                        values.get().getValues().length - (1 + tenHz));

                epochMax = DoubleMath.mean(vals);
                if (epochMax == 0)
                {
                    epochMax = 1;
                }
                return values.get().getValues().length - tenHz;
            }
            return 0;
        }

        @Override
        public Number getX(int index)
        {
            if (values.isPresent())
            {
                return values.get().getResolution() * index;

            }
            return 0;
        }

        @Override
        public Number getY(int index)
        {
            // avoid showing carrier frequencies
            if (values.isPresent() && index > 4 / values.get().getResolution())
            {

                double y = values.get().getValues()[index] / epochMax;
                return y;
            }
            return 0;
        }
    }


}
