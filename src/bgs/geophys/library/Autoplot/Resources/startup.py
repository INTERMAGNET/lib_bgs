# startup.py
# 
# This script will be passed to autoplot at startup - put function definitions in here so that
# they are available for use via the command interface, also global configuration commands

import autoplotapp
import autoplot


# function to load Geomagnetic data sets from a URI
# Input parameters: uri - a URI specifying the data to load, which could be of the form
#                             'vap+cdf:file:/C:/data/esk20050101_0000_4.cdf?'
#                         note that the final '?' introducing the query string is needed
# Returns: an array of data sets, one per plottable element in the file
def getGeomagDataSets (uri):
    """ load the elements from a geomagnetic CDF to an autoplot data set """

    available_uris = autoplotapp.getCompletions (uri)
    geomag_ds = []
    
    # iterate over the possible URIs for this data file
    for uri in available_uris:
        # find the URIs that contain geomagnetic data
        list = uri.split ('GeomagneticField')
        if len (list) == 2:
            # load the data to a data set
            geomag_ds.append (autoplot.getDataSet (uri))
            
    return geomag_ds

# show a full year plot
def showYearPlot (geomag_ds):
    """ plot the elements from a geomagnetic dataset in consecutive windows """
    count = 0
    for ds in geomag_ds:
        autoplotapp.plot (count, ds)
        count += 1

# show a 24 hour plot of data from an annual data set - plots are displayed in
# consecutive plot windows starting at 1
# Input parameters: geomag_ds - an array, each element containing a plottable annual data set
#                   day_no - the day number to display from the data sets
#                   samps_per_day - number of smaples per day
def showDayPlot (geomag_ds, day_no, samps_per_day):
    """ plot the elements from a geomagnetic dataset in consecutive windows """
    count = 0
    for ds in geomag_ds:
        day_ds = ds [(day_no -1) * samps_per_day:(day_no * samps_per_day)-1]
        autoplotapp.plot (count, day_ds)
        count += 1

