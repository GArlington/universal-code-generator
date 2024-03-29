// Copyright (c) 2011 Varner Lab

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

//
//  OutlineViewDelegate.h
//  Translator
//
//  Created by Jeffrey Varner on 1/20/11.


#import <Cocoa/Cocoa.h>
#import "TreeIconModel.h"
#import "DDTreeNodeProxy.h"



@interface OutlineViewDelegate : NSObject <NSOutlineViewDelegate> {

	@private
	TreeIconModel *iconModel;						// Icon model -
    IBOutlet NSOutlineView *treeView;               // Pointer to the specification tree -
    NSXMLElement *selectedXMLNode;
    
}

@property (retain) TreeIconModel *iconModel;
@property (retain) NSOutlineView *treeView;
@property (retain) NSXMLElement *selectedXMLNode;

// Called to customize tree cells -
-(NSCell *)outlineView:(NSOutlineView *)outlineView dataCellForTableColumn:(NSTableColumn *)tableColumn item:(id)item;
-(BOOL)outlineView:(NSOutlineView *)outlineView shouldEditTableColumn:(NSTableColumn *)tableColumn item:(id)item;


@end
