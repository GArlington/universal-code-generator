//
//  VLButton.m
//  Translator
//
//  Created by Jeffrey Varner on 3/15/11.
//  Copyright 2011 Varnerlab. All rights reserved.
//

#import "VLButton.h"


@implementation VLButton

- (id)init
{
    self = [super init];
    if (self) {
        // Initialization code here.
        
    }
    
    return self;
}

- (void)dealloc
{
    [super dealloc];
}

- (void)updateTrackingAreas
{
	[super updateTrackingAreas];
	
	if (trackingArea)
	{
		[self removeTrackingArea:trackingArea];
		[trackingArea release];
	}
	
	NSTrackingAreaOptions options = NSTrackingInVisibleRect | NSTrackingMouseEnteredAndExited | NSTrackingActiveInKeyWindow;
	trackingArea = [[NSTrackingArea alloc] initWithRect:NSZeroRect options:options owner:self userInfo:nil];
	[self addTrackingArea:trackingArea];
}

- (void)mouseEntered:(NSEvent *)event
{
}

- (void)mouseExited:(NSEvent *)event
{
}


@end
