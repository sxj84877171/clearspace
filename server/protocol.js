//////////////////////////////////////////////////////////////////////////////////////
// pc broadcast packet
{
    "v": "1.0",			// version
    "name": "james",	// packet type, request or response.
    "ip": "192.168.1.100",			// id.
    "status": "0"		
}
// start/stop download packet,send to pc
{
    "v": "1.0",			// version
    "path": "/sdcard/dcim/filelist.json",	// packet type, request or response.
    "devicename": "xt106",			// id.
    "deviceid": "dx0sdfsdfds"		
}

// file list package, send to pc for download 
{"/storage/emulated/0/DCIM/Camera/Alternate":
	[
		{"date":"1424490845000","filename":"IMG_20150221_115404452.jpg","size":2593825},
		{"date":"1424490908000","filename":"IMG_20150221_115507813.jpg","size":2534906}
	]
},
{"/storage/emulated/0/DCIM/Camera/test":
	[
		{"date":"1424490845000","filename":"IMG_20150221_115404452.jpg","size":2593825},
		{"date":"1424490908000","filename":"IMG_20150221_115507813.jpg","size":2534906}
	]
}

// http download request, send to phone
 url : http://192.168.111.113:7084/download/storage/emulated/0/DCIM/Camera/Alternate/IMG_20150221_115404452.jpg 

// http download file complete request, send to phone.
 url : http://192.168.111.113:7084/downloadcomplete/storage/emulated/0/DCIM/Camera/Alternate/IMG_20150221_115404452.jpg
 