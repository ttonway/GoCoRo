//
//  XYMarkerView.swift
//  ChartsDemo
//  Copyright © 2016 dcg. All rights reserved.
//

import Foundation
import Charts

open class XYMarkerView: BalloonMarker
{
    open override func refreshContent(entry: ChartDataEntry, highlight: Highlight)
    {
        setLabel(Bridge.formatPlotData(entry.data as! [AnyHashable : Any]!))
    }
    
}
