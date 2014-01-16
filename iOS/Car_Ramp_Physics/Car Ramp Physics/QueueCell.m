//
//  QueueCell.m
//  Car Ramp Physics
//
//  Created by Virinchi Balabhadrapatruni on 7/23/13.
//  Copyright (c) 2013 ECG. All rights reserved.
//

#import "QueueCell.h"

@implementation QueueCell

@synthesize dataSet, mKey;

- (QueueCell *)setupCellWithDataSet:(QDataSet *)ds andKey:(NSNumber *)key {
    self.mKey = key;
    
    dataSet = ds;
    
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    if (self) {
        self.contentView.backgroundColor = [UIColor clearColor];
    }
    return self;
}

- (void) setSessionName:(NSString *)name {
    [dataSet setName:name];
}

- (NSNumber *)getKey {
    return mKey;
}

- (void) setExpNum:(NSString *)exp {
        
    [dataSet setProjID:[NSNumber numberWithInt:[exp intValue]]];
}

- (BOOL) dataSetHasInitialExperiment {
    NSNumber *initial = [dataSet hasInitialProj];
    return [initial boolValue];
}



@end
