/*
Copyright (C) 2014 
"JOANNEUM RESEARCH Forschungsgesellschaft mbH" 
 Graz, Austria, digital-iis@joanneum.at.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package eu.eexcess.dataquality.graphs;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.plot.DefaultDrawingSupplier;

public class ChartDrawingSupplier extends DefaultDrawingSupplier  {

	private static final long serialVersionUID = -7755725418060445037L;
	public Paint[] paintSequence;
    public int paintIndex;
    public int fillPaintIndex;

    {
        paintSequence =  new Paint[] {
                new Color(227, 26, 28),
                new Color(000,102, 204),
                new Color(102,051,153),
                new Color(102,51,0),
                new Color(156,136,48),
                new Color(153,204,102),
                new Color(153,51,51),
                new Color(102,51,0),
                new Color(204,153,51),
                new Color(0,51,0),
        };
    }

    @Override
    public Paint getNextPaint() {
        Paint result = paintSequence[paintIndex % paintSequence.length];
        paintIndex++;
        return result;
    }


    @Override
    public Paint getNextFillPaint() {
        Paint result = paintSequence[fillPaintIndex % paintSequence.length];
        fillPaintIndex++;
        return result;
    }   
}