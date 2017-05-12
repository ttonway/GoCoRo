//
//  BalloonMarker.swift
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


open class BalloonMarker: MarkerImage
{
    open var leftImage: UIImage?
    open var rightImage: UIImage?
    open var font: UIFont?
    open var textColor: UIColor?
    open var insets = UIEdgeInsets()
    open var minimumSize = CGSize()
    
    fileprivate var labelns: NSString?
    fileprivate var _labelSize: CGSize = CGSize()
    fileprivate var _paragraphStyle: NSMutableParagraphStyle?
    fileprivate var _drawAttributes = [String : AnyObject]()
    
    public init(font: UIFont, textColor: UIColor, insets: UIEdgeInsets)
    {
        super.init()
        
        let left = UIImage(named:"line_marker_left")
        let right = UIImage(named:"line_marker_right")
        self.leftImage = left?.resizableImage(withCapInsets: UIEdgeInsets(top: 4, left: 12, bottom: 12, right: 5))
        self.rightImage = right?.resizableImage(withCapInsets: UIEdgeInsets(top: 4, left: 5, bottom: 12, right: 12))
        self.minimumSize = CGSize(width: 102, height: 34)
        
        self.font = font
        self.textColor = textColor
        self.insets = insets
        
        _paragraphStyle = NSParagraphStyle.default.mutableCopy() as? NSMutableParagraphStyle
        _paragraphStyle?.alignment = .center
    }
    
    open override func offsetForDrawing(atPoint point: CGPoint) -> CGPoint
    {
        let size = self.size
        var point = point
        point.x -= size.width / 2.0
        point.y -= size.height
        return super.offsetForDrawing(atPoint: point)
    }
    
    open override func draw(context: CGContext, point: CGPoint)
    {
        if labelns == nil
        {
            return
        }
        
        let offset = self.offsetForDrawing(atPoint: point)
        let size = self.size
        
        var rect = CGRect(
            origin: CGPoint(
                x: point.x + offset.x,
                y: point.y + offset.y),
            size: size)
        rect.origin.x -= size.width / 2.0
        rect.origin.y -= size.height
        
        context.saveGState()

        var half = rect
        half.size.width = half.size.width / 2
        UIGraphicsPushContext(context)
        leftImage!.draw(in: half)
        half.origin.x += half.size.width
        rightImage!.draw(in: half)
        UIGraphicsPopContext()
        
        rect.origin.y += self.insets.top
        rect.size.height -= self.insets.top + self.insets.bottom
        
        UIGraphicsPushContext(context)
        labelns?.draw(in: rect, withAttributes: _drawAttributes)
        UIGraphicsPopContext()
        
        context.restoreGState()
    }
    
    open override func refreshContent(entry: ChartDataEntry, highlight: Highlight)
    {
        setLabel(String(entry.y))
    }
    
    open func setLabel(_ label: String)
    {
        labelns = label as NSString
        
        _drawAttributes.removeAll()
        _drawAttributes[NSFontAttributeName] = self.font
        _drawAttributes[NSParagraphStyleAttributeName] = _paragraphStyle
        _drawAttributes[NSForegroundColorAttributeName] = self.textColor
        
        _labelSize = labelns?.size(attributes: _drawAttributes) ?? CGSize.zero
        
        var size = CGSize()
        size.width = _labelSize.width + self.insets.left + self.insets.right
        size.height = _labelSize.height + self.insets.top + self.insets.bottom
        size.width = max(minimumSize.width, size.width)
        size.height = max(minimumSize.height, size.height)
        self.size = size
    }
}
