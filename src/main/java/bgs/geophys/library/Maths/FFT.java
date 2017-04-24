/*
 * FFT.java
 *
 * Created on 03 September 2003, 15:38
 */

package bgs.geophys.library.Maths;

import java.io.*;

/************************************************************************
 * A class that implements a fast fourier transform.
 *
 * @authors  Iestyn Evans/Tom Shanahan
 ***********************************************************************/
public class FFT {
    
    // Public constants
    
    /************************************************************************
     *
     * Represents available windowing algorithms
     *
     ***********************************************************************/
    public static final int WINDOW_RECTANGLE = 0,
                            WINDOW_HANNING = 1,
                            WINDOW_WELCH = 2,
                            WINDOW_BARTLETT = 3;
    
    /************************************************************************
     *
     * Represents available specrtum normalizing algorithms
     *
     ***********************************************************************/
    public static final int NORMALIZE_SUM = 0,
                            NORMALIZE_MEAN = 1;
    
    // Private variables
    private int nPasses;
    private int windowType = WINDOW_RECTANGLE;
    private int normalizeType = NORMALIZE_MEAN;
    private double sinTable[];
    private double cosTable[];
    private boolean PASCAL = false;
    
    // class data fields
    private int nPoints;    
    

    
    /************************************************************************
     * Creates a new FFT object set up to use 2^power number of points.
     *
     * @param power The 'power' to use when computing FFT's 
     ***********************************************************************/
    @SuppressWarnings("unchecked")
    public FFT(int power)
    {   
        // Set number of passes in FFT        
        this.nPasses = power;
        this.nPoints = (int)Math.pow(2,power);
        // initialise class data fiels        
        
        /* Uncomment if tables are to be used - also uncomment lines in algorithms
        // Create sin and cos tables
        int size = (int) Math.pow(2, nPasses - 1);
        int N = size << 2;
        double constant;
        
        // Create sin an cos tables
        cosTable = new double[size];
        sinTable = new double[size];
        
        for (int n = 0; n < size; n++)
        {
            constant = -2 * Math.PI * n / N;
            cosTable[n] = Math.cos(constant);
            sinTable[n] = Math.sin(constant);
        }
         **/
    }
    
    
    /************************************************************************
     * Caclulates the forward real FFT.
     *
     * @param real The array containing the real parts of the data.
     * @param imaginary The array containing the imaginary parts of the data.
     *          This array should be blank on entry.
     ************************************************************************/
//    public void forwardRealFFT(double[] real, double[] imaginary)
//    {
//        // Uses a two for one complex transform.
//  
//        // Assign new arrays
//        int length = (int) Math.pow(2, nPasses);
//        double[] newReal = new double[length];
//        double[] newImaginary = new double[length];
//        
//        // Move data into new arrays
//        for (int i = 0; i < length; i++)
//        {
//            newReal[i] = real[i];
//            newImaginary[i] = real[i + length];
//        }
//        
//        // Perform a complex FFT
//        forwardFFT(newReal, newImaginary);
//        
//        // Bitreverse
//        //bitReverse(newReal, newImaginary);
//        
//        // Untangle the results
//        // Special case for i = 0
//        real[length] = newImaginary[0];
//        imaginary[length] = 0;
//            
//        // Second half
//        real[0] = newReal[0];
//        imaginary[0] = 0;
//        
//        // Compute the rest
//        for (int i = 1; i < length; i++)
//        {
//            // First half
//            real[i] = (newReal[i] + newReal[length - i]) / 2;
//            imaginary[length - i] = (-newImaginary[i] + newImaginary[length - i]) / 2;
//            
//            // Second half
//            real[length + i] = (newImaginary[i] + newImaginary[length - i]) / 2;
//            imaginary[length + i] = (newReal[length - i] - newReal[i]) / 2;
//        }
//    }
        
        
    
    /************************************************************************
     * Caclulates the forward complex FFT.
     *
     * @param real The array containing the real parts of the data.
     * @param imaginary The array containing the imaginary parts of the data.
     * @param removeMean Enables removal of mean
     ************************************************************************/
    public void forwardFFT(double[] real, double[] imaginary, boolean removeMean)
    {      
        // remove mean
        if(removeMean)removeMean(real);       
        
        // window the data according to the window function set
        window(real);
        
        // Selects one of the implementations        
        if (PASCAL)
            forwardFFTPASCAL(real, imaginary);
        else
            forwardFFTBASIC(real, imaginary);          
    }  
    
    
     /************************************************************************
     * Caclulates the forward complex FFT. Priavate method used by inverseFFT
     * for computing the inverse FFT without removal of mean or windowing.
     * 
     * @param real The array containing the real parts of the data.
     * @param imaginary The array containing the imaginary parts of the data.
     ************************************************************************/
    private void forwardFFT(double[] real, double[] imaginary)
    {
        // Selects one of the implementations        
        if (PASCAL)
            forwardFFTPASCAL(real, imaginary);
        else
            forwardFFTBASIC(real, imaginary);   
    }
        
    
    /************************************************************************
     * Caclulates the inverse complex FFT.
     *
     * @param real The array containing the real parts of the data.
     * @param imaginary The array containing the imaginary parts of the data.
     ************************************************************************/
    public void inverseFFT(double[] real, double[] imaginary)
    {                        
        // Selects one of the implementations
        if (PASCAL)
            inverseFFTPASCAL(real, imaginary);
        else
            inverseFFTBASIC(real, imaginary);
        
    }
                
    
    /************************************************************************
     * Caclulates an FFT (translated from PASCAL code) using a DIF algorithm.
     *      Takes input in standard order and outputs as bit reversed.
     *
     * @param real The array containing the real parts of the data.
     * @param imaginary The array containing the imaginary parts of the data.
     * @param forward True to perform a forward transform, false if reverse.
     ************************************************************************/
    private void fFTPASCALDIF(double[] real, double[] imaginary, boolean forward)
    {
        /* 
         * Translated from PASCAL like code found at http://www.eptools.com/tn/T0001/PT03.HTM#Head33
         *
         * Note 2^p = N = size of complex array.
         *
         * The function is a non-recursive Radix 2 DIF FFT.
         * It performs the following:
         *      Pass loop: Performs p 'passes' where P=0,1,2..,p-1.
         *      Block loop: Pass P operates on 2^P subblocks, each of size 2^(p-P).
         *      Butterfly loop: Each subblock performs 2^(p-P-1) butterflies.
         */                
        
        // Local variables
        int Bp;
        int Np;
        int Npi;
        int P;
        int p;
        int b;
        int n;
        int BaseE;
        int BaseO;
        complex e = new complex();
        complex o = new complex();
        complex tempe = new complex();
        complex tempo = new complex();
        complex factor;
        //int twiddleStepSize;
        
        // Calculate p (= N^2)
        p = nPasses;
        
        // Initialise pass parameters
        Bp = 1;
        Np = 1 << p;
        //twiddleStepSize = 1;
        
        // Pass Loop
        for (P = 0; P < p; P++)
        {
            Npi = Np >> 1;      // Number of butterflies
            BaseE = 0;
            
            // Block loop
            for (b = 0; b < Bp; b++)
            {
                BaseO = BaseE + Npi;
                
                // Butterfly loop
                for (n = 0; n < Npi; n++)
                {
                    // Calculate 'twiddle factor'
                    factor = twiddleFactor(Np,n, forward);
                    //factor = twiddleFactor(Np,n, forward, twiddleStepSize);
                    
                    // Initialise temporary complex numbers
                    tempe.real = real[BaseE+n];
                    tempe.imaginary = imaginary[BaseE+n];
                    tempo.real = real[BaseO+n];
                    tempo.imaginary = imaginary[BaseO+n];
                    
                    // Calculate real and imaginary parts
                    e.real = tempe.real + tempo.real;
                    e.imaginary = tempe.imaginary + tempo.imaginary;
                    o.real = ((tempe.real - tempo.real) * factor.real) -
                             ((tempe.imaginary - tempo.imaginary) * factor.imaginary);
                    o.imaginary = ((tempe.real - tempo.real) * factor.imaginary) +
                                  ((tempe.imaginary - tempo.imaginary) * factor.real);
                    
                    // Add results to arrays
                    real[BaseE+n] = e.real;
                    real[BaseO+n] = o.real;
                    imaginary[BaseE+n] = e.imaginary;
                    imaginary[BaseO+n] = o.imaginary;
                }
                // Calculate start of next block loop
                BaseE = BaseE + Np;
            }
            // Calculate parameters for next pass
            Bp = Bp << 1;
            Np = Np >> 1;
            //twiddleStepSize = twiddleStepSize << 1;
        }    
        
        // Normalise
        if (!forward)
        {
            int N = real.length;
            for (int count = 0; count < N; count++)
            {
                real[count] = real[count] / N;
                imaginary[count] = imaginary[count] / N;
            }
        }
    }
    
    /************************************************************************
     * Caclulates the forward FFT (translated from PASCAL code)
     *
     * @param real The array containing the real parts of the data.
     * @param imaginary The array containing the imaginary parts of the data.
     ************************************************************************/
    private void forwardFFTPASCAL(double[] real, double[] imaginary)
    {
        this.fFTPASCALDIF(real, imaginary, true);
        bitReverse(real,imaginary);
    }
    
    /************************************************************************
     * Caclulates the inverse FFT (translated from PASCAL code)
     *
     * @param real The array containing the real parts of the data.
     * @param imaginary The array containing the imaginary parts of the data.
     ************************************************************************/
    private void inverseFFTPASCAL(double[] real, double[] imaginary)
    {   
        this.fFTPASCALDIT(real, imaginary, false);
        bitReverse(real,imaginary);
    }
    
    /************************************************************************
     * Caclulates the twiddle factors for the PASCAL based FFT.
     *
     * @param N The number of points on the complex array.
     * @param n The index of the current point.
     * @param forward True to perform a forward transform, false if reverse.
     ************************************************************************/
    private complex twiddleFactor(int N, int n, boolean forward)
    {
        // TN(n) = e^(-i*2*PI*n/N) = cos (-2 * PI * n / N) + i sin (-2 * Pi * n / N)
        
        complex c = new complex();
        
        double constant = -2 * Math.PI * n / N;
        c.real = Math.cos(constant);
        c.imaginary = Math.sin(constant);
        
        // If reverse transform then return complex conjugate
        if (!forward) c.imaginary = -c.imaginary;

        return c;
    }
    
    /************************************************************************
     * Caclulates the twiddle factors for the PASCAL based FFT.
     *
     * @param N The number of points on the complex array.
     * @param n The index of the current point.
     * @param forward True to perform a forward transform, false if reverse.
     * @param stepSize The step size used while traversing the sin array.
     ************************************************************************/
    private complex twiddleFactor(int N, int n, boolean forward, int stepSize)
    {
        // TN(n) = e^(-i*2*PI*n/N) = cos (-2 * PI * n / N) + i sin (-2 * Pi * n / N)
        
        complex c = new complex();

        // NEW CODE --- retrieve values from tables
        int index = n * stepSize;
        c.real = cosTable[index];
        c.imaginary = sinTable[index];
        
        // If reverse transform then return complex conjugate
        if (!forward) c.imaginary = -c.imaginary;

        return c;
    }
    
    /************************************************************************
     * Caclulates an FFT using a DIT from pascal translated code.
     *      Takes input in bit reversed order and outputs as standard.
     *
     * @param real The array containing the real parts of the data.
     * @param imaginary The array containing the imaginary parts of the data.#
     * @param forward True to perform a forward transform, false if reverse.
     ************************************************************************/
    private void fFTPASCALDIT(double[] real, double[] imaginary, boolean forward)
    {
        // Local variables
        int Bp;
        int Np;
        int Npi;
        int P;
        int p;
        int b;
        int k;
        int BaseT;
        int BaseB;
        //int twiddleStepSize;
        complex top = new complex();
        complex bot = new complex();
        complex temptop = new complex();
        complex tempbot = new complex();
        complex factor;
        
        // Calculate p
        p = this.nPasses;
        
        // Inititalse pass parameters
        Bp = 1<<(p-1);
        Np = 2;
        //twiddleStepSize = Bp;
        
        // Pass loop
        for (P=0; P < p; P++)
        {
            Npi = Np>>1;
            BaseT=0;

            
            // Block loop
            for (b=0; b<Bp; b++)
            {
                BaseB = BaseT+Npi;
                
                // Butterfly loop
                for (k=0; k<Npi; k++)
                {
                    // Calculate twiddle factor
                    //factor = twiddleFactor(Np,k,forward,twiddleStepSize);
                    factor = twiddleFactor(Np,k,forward);
                    
                    // Calculate top and bottom complex variables
                    top.real = real[BaseT+k];
                    top.imaginary = imaginary[BaseT+k];
                    bot.real = (real[BaseB+k] * factor.real) - 
                               (imaginary[BaseB+k] * factor.imaginary);
                    bot.imaginary = (real[BaseB+k] * factor.imaginary) + 
                                    (imaginary[BaseB+k] * factor.real);
                    
                    // Add results to array
                    real[BaseT+k] = top.real + bot.real;
                    imaginary[BaseT+k] = top.imaginary + bot.imaginary;
                    real[BaseB+k] = top.real - bot.real;
                    imaginary[BaseB+k] = top.imaginary - bot.imaginary;
                }
                BaseT = BaseT + Np;
            }
            Bp = Bp>>1;
            Np = Np<<1;
            // twiddleStepSize = twiddleStepSize>>1;
        }
        
        // Normalise
        if (!forward)
        {
            int N = real.length;
            for (int count = 0; count < N; count++)
            {
                real[count] = real[count] / N;
                imaginary[count] = imaginary[count] / N;
            }
        }
    }
    
    /************************************************************************
     * Caclulates the forward FFT (translated from BASIC code)
     *
     * @param real The array containing the real parts of the data.
     * @param imaginary The array containing the imaginary parts of the data.
     ************************************************************************/
    private void forwardFFTBASIC(double[] real, double[] imaginary)
    {        
        
        // Set constants
        int N = real.length;
        int NM1 = N - 1;
        int ND2 = (int) N / 2;
        int M = (int) (Math.log(N) / Math.log(2));
        int J = ND2;
        
        double TR, TI;
        
        
        for (int I = 1; I < N-1; I++)
        {
         // Bit reversal sorting
            if (I < J)
            {
                TR = real[J];
                TI = imaginary[J];
                real[J] = real[I];
                imaginary[J] = imaginary[I];
                real[I] = TR;
                imaginary[I] = TI;
            }
            
            int K = ND2;
            
            while (K <= J)
            {
                J = J - K;
                K = (int) (K / 2);
            }
            
            J = J + K;
        }
        
        // Loop for each stage
        for (int L = 1; L < M + 1; L++)
        {
            int LE = (int)(Math.pow(2, L));
            int LE2 = (int)(LE / 2);
            double UR = 1;
            double UI = 0;
            double SR = Math.cos(Math.PI / LE2);
            double SI = - Math.sin(Math.PI / LE2);
            
            for (J = 1; J < LE2 + 1; J++)
            {
             // Loop for each DFT
                int JM1 = J-1;
                for (int I = JM1; I < NM1 + 1; I = I + LE)
                {
                    int IP = I + LE2;
                    TR = real[IP] * UR - imaginary[IP] * UI;
                    TI = real[IP] * UI + imaginary[IP] * UR;
                    real[IP] = real[I] - TR;
                    imaginary[IP] = imaginary[I] - TI;
                    real[I] = real[I] + TR;
                    imaginary[I] = imaginary[I] + TI;
                }
                TR = UR;
                UR = (TR*SR - UI*SI);
                UI = (TR*SI + UI*SR);
            }
        }
    }
    
    /************************************************************************
     * Caclulates the inverse FFT
     *
     * @param real The array containing the real parts of the data.
     * @param imaginary The array containing the imaginary parts of the data.
     ************************************************************************/
    private void inverseFFTBASIC(double[] real, double[] imaginary)
    {
        // The function does some changing of the components so that the forward FFT's
        // code can be used again. The returned values are reordered to obtain the result.
        
        for (int i = 0; i < imaginary.length; i++)
        {
            // Change sign of the imaginary parts
            imaginary[i] = - imaginary[i];
        }
        
        forwardFFT(real, imaginary);
        
        for (int i = 0; i < imaginary.length; i++)
        {
            // Divide the time domain by the number of complex numbers
            // and change the sign of the imaginary parts
            real[i] = (real[i] / real.length);
            imaginary[i] = -(imaginary[i] / imaginary.length);
        }
    }
    
    /************************************************************************
     * Rearranges the arrays into bit-reversed order.
     *
     * @param real The array containing the real parts of the data.
     * @param imaginary The array containing the imaginary parts of the data.
     ************************************************************************/
    public void bitReverse(double[] real, double[] imaginary)
    {
        int n = real.length;
        int nD2 = (int) n / 2;
        int j = nD2;
        
        double tempr, tempi;
        
        for (int i = 1; i < n-1; i++)
        {
         // Bit reversal sorting
            if (i < j)
            {
                tempr = real[j];
                tempi = imaginary[j];
                real[j] = real[i];
                imaginary[j] = imaginary[i];
                real[i] = tempr;
                imaginary[i] = tempi;
            }
            
            int k = nD2;
            
            while (k <= j)
            {
                j = j - k;
                k = (int) (k / 2);
            }
            
            j = j + k;
        }
    }
    
    /************************************************************************
     * Calculates a power spectrum from a set of real and imaginary time series
     * data points.
     *
     * @param real The array containing the real time series data points.
     * @param imaginary The array containing the imaginary time series points (zeros)
     * 
     * @return The power spectrum as an array of N/2 length. With values running to
     *              N/2 - 1 
     *             The values are from index 1 to the nyquist frequency - the
     *             zero'th term is not included.
     ************************************************************************/
//    public double[] powerSpectrum(double[] real, double[] imaginary)
//    {
//        double[] spectrum;       
//        
//        // Remove mean
//        if (removeMean)
//        {
//            double average = 0;
//            for (int count=0; count<real.length; count++)
//            {
//                average += real[count];
//            }
//        
//            average = average / real.length;
//        
//            for (int count=0; count<real.length; count++)
//            {
//                real[count] = real[count] - average;
//            }
//        }
//        
//        
//        // Window the data
//        window(real);
//        
//        // Calculate fourier series from the windowed data
//        forwardFFT(real, imaginary);                
//        
//        // Bitreverse
//        //bitReverse(data, complex);
//        
//        // Remove the N+1th value stored in imag[0]
//        //complex[0] = 0;
//        //System.err.println(data[0] + "  " + complex[0]);
//        
//        // Create output array and calculate spectrum
//        //spectrum = data;
//        spectrum = new double[(real.length / 2)];
//        
//        for (int count = 0; count < (real.length / 2); count++)
//        {
//            spectrum[count] = (real[count + 1] * real[count + 1]) + (imaginary[count + 1] * imaginary[count + 1]); // A^2 + B^2 = C^2 = power
//            spectrum[count] = Math.pow(spectrum[count], 0.5);
//        }
//        
//        // if using hanning window a x2 factor has been added to recover correct 
//        // amplitudes, which are effectively halved when using a hanning window (tjgs) 
//        if(windowType == FFT.WINDOW_HANNING){
//            for(int i=0; i<spectrum.length; i++) spectrum[i] = spectrum[i] * 2;            
//        }
//        
//        
//        /** Debug stuff
//        double largestValue = spectrum[0];
//        int largestIndex = 0;
//        double average = 0;
//        for (int count = 0; count < spectrum.length; count++)
//        {
//            average += spectrum[count];
//            if (spectrum[count] > largestValue)
//            {
//                largestValue = spectrum[count];
//                largestIndex = count;
//            }
//        }
//        average = average / spectrum.length;
//        System.out.println(largestIndex + " " + largestValue + " " + average + " " + (largestValue / average));
//        */
//        
//        // Normalise
//        for (int count = 0; count < spectrum.length; count++)
//        {
//            if (normalizeType == NORMALIZE_MEAN)
//            {
//                // Use mean squared amplitude   
//                spectrum[count] = spectrum[count] / spectrum.length; 
//            }
//            else if (normalizeType == NORMALIZE_SUM)
//            {
//                // For sum squared amplitude nothing needs to be done
//            }
//            else
//            {
//                // An invalid type has been selected assume sum squared amplitude
//            }
//            if (OUTPUT_LOG)
//            {
//                if (spectrum[count] > 0)
//                {
//                    // Not clear why gain is being calculated like this?
//                    //spectrum[count] = (Math.log(spectrum[count]) + 10) * 10;
//                    // Redefined by tjgs on 11/2007
//                    spectrum[count] = 20*Math.log10(spectrum[count]);
//                   
//                }
//            }
//        }
//        
//        return spectrum;
//    }
    
    /************************************************************************
     * Returns the imaginary data from the polar form.
     *
     * @param magnitude The real data.
     * @param phase The imaginary data.
     * @return The imaginary data.
     ************************************************************************/
    public double[] getImaginary(double[] magnitude, double[] phase){         
        double[] imaginary = new double[magnitude.length];        
        for(int count=0; count<imaginary.length; count++){            
            imaginary[count] = magnitude[count] * Math.sin(phase[count]);
        }   
        return imaginary;
    }
    
    /************************************************************************
     * Returns the real data from the polar form.
     *
     * @param magnitude The real data.
     * @param phase The imaginary data.
     * @return The real data.
     ************************************************************************/
    public double[] getReal(double[] magnitude, double[] phase){
        double[] real = new double[magnitude.length];   
        for(int count=0; count<real.length; count++){        
            real[count] = magnitude[count] * Math.cos(phase[count]);
        }
        return real;
    }
    
    /************************************************************************
     * Returns the magnitude data from the rectangular form.
     *
     * @param real The real data.
     * @return The magnitude data
     * @param imaginary The imaginary data.
     ************************************************************************/
    public double[] getMagnitude(double[] real, double[] imaginary){
        double magnitude[] = new double[real.length];              
        for(int count=0; count<magnitude.length; count++){
            magnitude[count] = Math.pow(real[count], 2) + Math.pow(imaginary[count], 2);
            magnitude[count] = Math.pow(magnitude[count], 0.5);            
        }
        return magnitude;
    }
    
     /************************************************************************
     * Returns the phase data from the rectangular form.
     *
     * @param real The real data.
     * @return The phase data.
     * @param imaginary The imaginary data.
     ************************************************************************/
    public double[] getPhase(double[] real, double[] imaginary){
        double phase[] = new double[real.length];
        for(int count=0; count<phase.length; count++){                       
            phase[count] = Math.atan2(imaginary[count], real[count]);            
        }
        return phase;
          
    }
    
     /************************************************************************
     * Returns a Logarithmic Power Spectrum of the magnitude data passed.
     * Any power loss due to windowing is corrected using a a correction 
     * factor.
     *
     * @param magnitude The polar magnitude data to convert.
     * @return The Logarithmic Power Spectrum
     ************************************************************************/
    public double[] getLogPowerSpectrum(double magnitude[])
    {
        double[] logPowerSpectrum = new double[magnitude.length];
        for (int count = 0; count < magnitude.length; count++){
            logPowerSpectrum[count] = magnitude[count];
            
            // if using hanning window a x2 factor has been added to recover correct 
            // amplitudes, which are effectively halved when using a hanning window (tjgs)   
            if(windowType == FFT.WINDOW_HANNING) logPowerSpectrum[count] = logPowerSpectrum[count] * 2;                             
  
            // calculate logarithmic output in dB of power
            if (logPowerSpectrum[count] > 0)
            {
                // Not clear why gain is being calculated like this?
                //spectrum[count] = (Math.log(spectrum[count]) + 10) * 10;
                // Redefined by tjgs on 11/2007
                logPowerSpectrum[count] = 20*Math.log10(logPowerSpectrum[count]);               
            }            
        }                                          
        return logPowerSpectrum;
    }        
    
    
//    /************************************************************************
//     * Calculates a power spectrum from a set of real data points.
//     *
//     * @param data The array containing the data points.
//     * @param windowType An integer representing the window algorithm to be
//     *                   used. This sets the class variable to the new
//     *                   algorithm as well.
//     * 
//     * @return The power spectrum as an array of half the length plus one as
//     *                   the data. The xeroth element is included.
//     ************************************************************************/
//    public double[] powerSpectrumWithZeroth(double[] data, int windowType)
//    {
//        // Set the class windowType
//        setWindowType(windowType);
//        
//        // Calculate the power spectrum
//        return powerSpectrum(data);
//    }
    
    
     /************************************************************************
     * Normalise rectangular data using the current normalisation function.
     * For NORMALISE_MEAN, real and imaginary data elements are divided by
     * N/2 factor. 
     *
     * @param real The real data to normalise.
     * @param imaginary The imaginary datat to normalise.
     ************************************************************************/
    public void normaliseRect(double[] real, double imaginary[]){
        // normalise        
        switch (normalizeType){
            // Use mean squared amplitude
            case NORMALIZE_MEAN:
                for(int count=0; count<real.length; count++){                    
                    real[count] = real[count] / (real.length/2); 
                    imaginary[count] = imaginary[count] / (imaginary.length/2);
                }
                // Special cases are N=0 and N=N/2 for real part only
                real[0] = real[0] / 2;
                real[real.length/2] = real[real.length/2] / 2;                    
                break;

            case NORMALIZE_SUM:
                // For sum squared amplitude nothing needs to be done
                break;                                   
        }                                            
    }
    
    
     /************************************************************************
     * Normalise magnitude data using the current normalisation function.
     * For NORMALISE_MEAN, real and imaginary data elements are divided by
     * N/2 factor. 
     *
     * @param magnitude The magnitude data to normalise.
     ************************************************************************/
    public void normaliseMagnitude(double[] magnitude){        
        switch (normalizeType){
            case NORMALIZE_MEAN:
                // Use mean squared amplitude
                for(int count=0; count<magnitude.length; count++){
                    magnitude[count] = magnitude[count] / (magnitude.length/2);
                }
                // Special cases are N=0 and N=N/2
                magnitude[0] = magnitude[0] / 2;
                magnitude[magnitude.length/2] = magnitude[magnitude.length/2] / 2;
                break;

            case NORMALIZE_SUM:
                // For sum squared amplitude nothing needs to be done
                break;   
        }
    }
    
    
     /************************************************************************
     * Averages passed data
     *
     * @param data The data to averge.
     ************************************************************************/
    public void removeMean(double[] data){
        double average = 0;
            for (int count=0; count<data.length; count++)
            {
                average += data[count];
            }
        
            average = average / data.length;
        
            for (int count=0; count<data.length; count++)
            {
                data[count] = data[count] - average;
            }
    }
    
    
    /************************************************************************
     * Windows data using the current the windowing function.
     *
     * @param data The data to window.
     ************************************************************************/
    public void window(double[] data)
    {
        if (windowType == WINDOW_RECTANGLE)
        {
            // Don't do anything
            return;
        }
        else if (windowType == WINDOW_HANNING)
        {
             windowHanning(data);
             return;   
        }
        else if (windowType == WINDOW_WELCH)
        {
            windowWelch(data);
            return;
        }
        else if (windowType == WINDOW_BARTLETT)
        {
            windowBartlett(data);
            return;
        }
        else
        {
            // An invalid windows has been selected - use the rectangular window
            // Therefore do nothing
            return;
        }
    }
    
    /************************************************************************
     * Returns windowed data using the hanning windowing function.
     *
     * @param data The data to window.
     ************************************************************************/
    private void windowHanning(double[] data)
    {
        for (int count = 0; count < data.length; count++)
        {            
            data[count] = data[count] * (0.5 * (1 - Math.cos((count * 2 * Math.PI) / data.length)));     
        }
    }
    
    /************************************************************************
     * Returns windowed data using the Welch windowing function.
     *
     * @param data The data to window.
     ************************************************************************/
    private void windowWelch(double[] data)
    {
        for (int count = 0; count < data.length; count++)
        {
            data[count] = data[count] * (1 - (Math.pow((count - (0.5 * data.length)) / (0.5 * data.length), 2)));
        }
    }
    
    /************************************************************************
     * Returns windowed data using the Bartlett windowing function.
     *
     * @param data The data to window.
     ************************************************************************/
    private void windowBartlett(double[] data)
    {
        for (int count = 0; count < data.length; count++)
        {
            data[count] = data[count] * (1 - (Math.abs((count - (0.5 * data.length)) / (0.5 * data.length))));
        }
    }
    
    /************************************************************************
     * Sets the window function to be used when calculating a power spectrum.
     *
     * @param windowType A valid WINDOW_XXX variable from the class
     ************************************************************************/
    public void setWindowType(int windowType)
    {
        this.windowType = windowType;
    }
    
    /************************************************************************
     * Sets the normalization to be used when calculating a power spectrum.
     *
     * @param normalizeType A valid NORMALIZE_XXX variable from the class
     ************************************************************************/
    public void setNormalizeType(int normalizeType)
    {
        this.normalizeType = normalizeType;
    }
        
    
    /************************************************************************
     * Prints information about the complex variables stored in the arrays.
     *
     * @param real The array containing the real parts of the data.
     * @param imaginary The array containing the imaginary parts of the data.
     ************************************************************************/
    public void print(double[] real, double[] imaginary, PrintStream stream)
    {        
        complex c = new complex();
        
        for (int i = 0; i < real.length; i++)
        {
            c.real = real[i];
            c.imaginary = imaginary[i];
            c.print(stream);
        }
        stream.println();
    }

    
    
        
    public class complex
    {
        public double real;
        public double imaginary;
        
        public complex()
        {
            real = 0;
            imaginary = 0;
        }
        
        public void print(PrintStream stream)
        {
            stream.print(real + "      ");
            stream.print(imaginary + "      ");
            stream.print(r() + "      ");
            stream.print(theta() + "      ");
            stream.println();
        }
        
        public double r()
        {
            return Math.sqrt((real * real) + (imaginary * imaginary));
        }
        
        public double theta()
        {
            return Math.atan2(real, imaginary);
        }
    } 
}
