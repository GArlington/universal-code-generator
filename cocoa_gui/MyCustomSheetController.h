// Copyright (c) 2011 Varner Lab, Chemical and Biomolecular Eng, Cornell 
// University

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
//  MyCustomSheetController.h
//  Translator
//
//  Created by Jeffrey Varner on 2/6/11.
//

#import <Cocoa/Cocoa.h>


@interface MyCustomSheetController : NSWindowController {

    @private
    IBOutlet NSWindow *applicationWindow;
    IBOutlet NSWindow *localWindow;
    IBOutlet NSTextField *textLabel;
    NSXMLElement *selectedXMLNode;
    
}

// Property -
@property (retain) NSWindow *applicationWindow;
@property (retain) NSWindow *localWindow;
@property (retain) NSTextField *textLabel;
@property (retain) NSXMLElement *selectedXMLNode;


// Action methods -
- (IBAction)closeCustomSheet:(NSButton *)sender;
- (IBAction)changeNodeName:(NSButton *)sender;
- (IBAction)showCustomSheet:(NSWindow *)window;
- (IBAction)updateNodeName:(NSButton *)sender;

// Method is required to close the sheet -
- (void)sheetDidEnd:(NSWindow *)sheet returnCode:(NSInteger)returnCode contextInfo:(void *)contextInfo;

@end
