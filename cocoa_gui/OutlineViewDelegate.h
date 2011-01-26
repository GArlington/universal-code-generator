//
//  OutlineViewDelegate.h
//  Translator
//
//  Created by Jeffrey Varner on 1/20/11.
//  Copyright 2011 Chemical and Biomolecular Engineering. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import "TreeIconModel.h"

@interface OutlineViewDelegate : NSObject <NSOutlineViewDelegate> {

	@private
	TreeIconModel *iconModel;						// Icon model -
}

@property (retain) TreeIconModel *iconModel;

// Called to customize tree cells -
-(NSCell *)outlineView:(NSOutlineView *)outlineView dataCellForTableColumn:(NSTableColumn *)tableColumn item:(id)item;

@end
