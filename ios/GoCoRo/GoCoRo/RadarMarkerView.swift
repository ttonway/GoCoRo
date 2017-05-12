//
//  RadarMarkerView.swift
//  ChartsDemo
//
//  Copyright 2015 Daniel Cohen Gindi & Philipp Jahoda
//  A port of MPAndroidChart for iOS
//  Licensed under Apache License 2.0
//
//  https://github.com/danielgindi/Charts
//

import Foundation
import Charts

open class RadarMarkerView: MarkerView
{
    @IBOutlet var label: UILabel?
    
    open override func awakeFromNib()
    {
        self.offset.x = -self.frame.size.width / 2.0
        self.offset.y = -self.frame.size.height - 4.0
    }
    
    open override func refreshContent(entry: ChartDataEntry, highlight: Highlight)
    {
        label?.text = Bridge.formatScore(entry.y)
        layoutIfNeeded()
    }
}
