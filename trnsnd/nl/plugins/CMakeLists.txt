cmake_minimum_required (VERSION 2.6) 
project (CAUGens)
project (NLUGens)

include_directories(${SC_PATH}/include/plugin_interface) 
include_directories(${SC_PATH}/include/common) 
#include_directories(${SC_PATH}/server/plugins) #optional, for FFT_UGens.h
include_directories(${SC_PATH}/external_libraries/libsndfile/) 

set(CMAKE_SHARED_MODULE_PREFIX "")
if(APPLE OR WIN32)
set(CMAKE_SHARED_MODULE_SUFFIX ".scx")
endif()

add_library(CAUGens MODULE CAUGens.cpp) 
add_library(NLUGens MODULE NLUGens.cpp) 

######################## to compile
######################## download supercollider src, 
######################## run (pointing to the src directory):
######################## cmake -DSC_PATH=/Users/yota/Downloads/sc366 .
######################## then
######################## make