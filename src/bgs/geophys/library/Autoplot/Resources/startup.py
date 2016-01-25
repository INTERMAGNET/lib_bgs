# startup.py
# 
# This script will be passed to autoplot at startup - put function definitions in here so that
# they are available for use via the command interface, also global configuration commands

import autoplotapp
import autoplot

# this next line allows scripts to use the same syntax to access the DOM
# as if they were executing in the autoplot script panel
dom = autoplotapp.getDocumentModel()

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

# show a full dataset plot
def showDSPlot (geomag_ds, title):
    """ plot the elements from a geomagnetic dataset in consecutive windows """
    count = 0
    for ds in geomag_ds:
        autoplotapp.plot (count, ds)
        count += 1
    removeTimeScales ()
    setPlotTitle (title)
    resizePlots ();


# show a 24 hour plot of data from a larger data set - plots are displayed in
# consecutive plot windows starting at 1
# Input parameters: geomag_ds - an array, each element containing a plottable annual data set
#                   day_no - the day number to display from the data sets
#                   samps_per_day - number of smaples per day
#                   title - the title for the plot
def showDayPlot (geomag_ds, day_no, samps_per_day, title):
    """ plot the elements from a geomagnetic dataset in consecutive windows """
    count = 0
    for ds in geomag_ds:
        day_ds = ds [(day_no -1) * samps_per_day:(day_no * samps_per_day)-1]
        autoplotapp.plot (count, day_ds)
        count += 1
    removeTimeScales ()
    setPlotTitle (title)
    resizePlots ();


# resize the plot elements - the way to do this was worked out emprically -
# there may be a beter way! Using the layout buttons in autoplot gets the
# same layout, but I haven't found the Jython interface to simulate the buttons
def resizePlots ():
    if len (dom.plots) == 3:
        top_settings =    ["+2.0em",       "37.40%",       "72.50%"]
        bottom_settings = ["37.40%-1.0em", "72.50%-1.0em", "112.30%-4.2em"]
    elif len (dom.plots) == 4:
        top_settings =    ["+2.0em",       "28.90%",       "55.80%",       "81.00%"]
        bottom_settings = ["28.90%-1.0em", "55.80%-1.0em", "81.00%-1.0em", "112.30%-4.2em"]
    else:
        return
        
    for count in range (0, len (dom.plots)):
        dom.plots[count].controller.row.top = top_settings[count]
        dom.plots[count].controller.row.bottom = bottom_settings[count]

    
# remove the time scale from all but the last plot in a window
def removeTimeScales ():
    """ remove the tick marks and annotation from the time axis of all but the last plot """
    for count in range (0, len (dom.plots) -1):
        dom.plots[count].controller.plot.xaxis.setVisible (0)

    
# attach a title to the first plot on the canvas
# resize plots
def setPlotTitle (title):
    """ set the title on the first plot in a window """
    dom.plots[0].controller.setTitleAutomatically(title)
