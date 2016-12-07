// IPlayService.aidl
package com.example.ch3_aidl;

// Declare any non-default types here with import statements

interface IPlayService {
   int currentPosition();
   int getMaxDuration();
   int start();
   int stop();
   int getMediaStatus();
}
