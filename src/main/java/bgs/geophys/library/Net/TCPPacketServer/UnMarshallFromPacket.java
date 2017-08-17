package bgs.geophys.library.Net.TCPPacketServer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Decode a set of packets encoded through MarshallToPacket. For details
 * of the format used in the packets see MarshallToPacket.
 * 
 * @author smf
 */
public class UnMarshallFromPacket
{
    
    public enum ArgType {String, Int, Date, ArrayString, ArrayInt, ArrayDate}
    
    private class Argument
    {
        private ArgType arg_type;
        private Object argument;
        public Argument (String arg_type_string, String argument_string, int parse_pos)
        throws ParseException
        {
            if (arg_type_string.equalsIgnoreCase("String"))
            {
                arg_type = ArgType.String;
                argument = argument_string;
            }
            else if (arg_type_string.equalsIgnoreCase("ArrayString"))
            {
                arg_type = ArgType.ArrayString;
                int arg_pos = 0;
                LengthString n_args = extractLengthString(argument_string, arg_pos);
                arg_pos += n_args.getLengthString().length() +1;
                String strings [] = new String [n_args.getLengthVal()];
                for (int count=0; count<strings.length; count++)
                {
                    LengthString ls = extractLengthString(argument_string, arg_pos);
                    arg_pos += ls.getLengthString().length() +1;
                    strings [count] = extractString(argument_string, argument_string.length(), arg_pos, ls.getLengthVal());
                    arg_pos += ls.getLengthVal();
                }
                argument = strings;
            }
            else if (arg_type_string.equalsIgnoreCase("Int"))
            {
                arg_type = ArgType.Int;
                try { argument = Integer.parseInt(argument_string); }
                catch (NumberFormatException e) { throw new ParseException ("Bad Int argument: " + argument_string, parse_pos); }
            }
            else if (arg_type_string.equalsIgnoreCase("ArrayInt"))
            {
                arg_type = ArgType.ArrayInt;
                int arg_pos = 0;
                LengthString n_args = extractLengthString(argument_string, arg_pos);
                arg_pos += n_args.getLengthString().length() +1;
                int int_vals [] = new int [n_args.getLengthVal()];
                for (int count=0; count<int_vals.length; count++)
                {
                    LengthString ls = extractLengthString(argument_string, arg_pos);
                    arg_pos += ls.getLengthString().length() +1;
                    try { int_vals [count] = Integer.parseInt(extractString(argument_string, argument_string.length(), arg_pos, ls.getLengthVal())); }
                    catch (NumberFormatException e) { throw new ParseException ("Bad ArrayInt element: " + argument_string, parse_pos + arg_pos); }
                    arg_pos += ls.getLengthVal();
                }
                argument = int_vals;
            }
            else if (arg_type_string.equalsIgnoreCase("Date"))
            {
                arg_type = ArgType.Date;
                try { argument = new Date (Long.parseLong(argument_string)); }
                catch (NumberFormatException e) { throw new ParseException ("Bad Date argument: " + argument_string, parse_pos); }
            }
            else if (arg_type_string.equalsIgnoreCase("ArrayDate"))
            {
                arg_type = ArgType.ArrayDate;
                int arg_pos = 0;
                LengthString n_args = extractLengthString(argument_string, arg_pos);
                arg_pos += n_args.getLengthString().length() +1;
                Date dates [] = new Date [n_args.getLengthVal()];
                for (int count=0; count<dates.length; count++)
                {
                    LengthString ls = extractLengthString(argument_string, arg_pos);
                    arg_pos += ls.getLengthString().length() +1;
                    try { dates [count] = new Date (Long.parseLong(extractString(argument_string, argument_string.length(), arg_pos, ls.getLengthVal()))); }
                    catch (NumberFormatException e) { throw new ParseException ("Bad ArrayDate element: " + argument_string, parse_pos + arg_pos); }
                    arg_pos += ls.getLengthVal();
                }
                argument = dates;
            }
            else
                throw new ParseException ("Unrecognisedd argument type: " + arg_type_string, parse_pos);
        }
        public Object getArgument () { return argument; }
        public ArgType getArgumentType () { return arg_type; }
        public String getArgumentString () 
        throws IllegalArgumentException
        { 
            if (arg_type != ArgType.String) throw new IllegalArgumentException();
            return (String) argument; 
        }
        public String [] getArgumentArrayString () 
        throws IllegalArgumentException
        { 
            if (arg_type != ArgType.ArrayString) throw new IllegalArgumentException();
            return (String []) argument; 
        }
        public int getArgumentInt () 
        { 
            if (arg_type != ArgType.Int) throw new IllegalArgumentException();
            return (int) argument; 
        }
        public int [] getArgumentArrayInt () 
        throws IllegalArgumentException
        { 
            if (arg_type != ArgType.ArrayInt) throw new IllegalArgumentException();
            return (int []) argument; 
        }
        public Date getArgumentDate () 
        { 
            if (arg_type != ArgType.Date) throw new IllegalArgumentException();
            return (Date) argument; 
        }
        public Date [] getArgumentArrayDate () 
        throws IllegalArgumentException
        { 
            if (arg_type != ArgType.ArrayDate) throw new IllegalArgumentException();
            return (Date []) argument; 
        }
    }
    
    private class LengthString
    {
        private String length_string;
        private int length_val;
        public LengthString (String length_string, int parse_pos)
        throws ParseException
        {
            if (length_string.length() <= 0) throw new ParseException ("Missing length string", parse_pos);
            try { length_val = Integer.parseInt(length_string); }
            catch (NumberFormatException e) { throw new ParseException ("Badly formatted length string: " + length_string, parse_pos); }
            this.length_string = length_string;
        }
        public String getLengthString () { return length_string; }
        public int getLengthVal () { return length_val; }
    }
    

    private String method_name;
    private List<Argument> arguments;
    private String errmsg;
    
    public UnMarshallFromPacket (String packet) 
    throws ParseException
    {
        // get the total length data from the message
        LengthString total_length_string = extractLengthString(packet, 0);
        int pos = total_length_string.getLengthString().length() + 1;
        int total_length = total_length_string.getLengthVal() + total_length_string.getLengthString().length() +1;
        
        // check there is enough data in the message
        if (packet.length() < total_length)
            throw new ParseException("Not enough data in packet", 0);
        
        // extract the method name and numbeer of arguments from the message data
        LengthString length_string = extractLengthString(packet, pos);
        pos += length_string.getLengthString().length() +1;
        method_name = extractString (packet, total_length, pos, length_string.getLengthVal());
        pos += length_string.getLengthVal();
        length_string = extractLengthString(packet, pos);
        int n_args = length_string.getLengthVal();
        pos += length_string.getLengthString().length() +1;
        
        // if n_args is < 0, the packet contains an error message
        arguments = new ArrayList<> ();
        if (n_args < 0)
        {
            length_string = extractLengthString(packet, pos);
            pos += length_string.getLengthString().length() +1;
            errmsg = extractString (packet, total_length, pos, length_string.getLengthVal());
            pos += length_string.getLengthVal();
        }
        else
        {
            errmsg = null;
            
            // extract the arguments from the message data
            for (int arg_count = 0; arg_count < n_args; arg_count ++)
            {
                String arg_type_string = findDelimitedString (packet, pos);
                int length_string_pos = pos + arg_type_string.length() +1;
                length_string = extractLengthString (packet, length_string_pos);
                int argument_pos = length_string_pos + length_string.getLengthString().length() +1;
                String argument = extractString (packet, total_length, argument_pos, length_string.getLengthVal());
                arguments.add (new Argument (arg_type_string, argument, pos));
                pos += arg_type_string.length() +1 + length_string.getLengthString().length() +1 + length_string.getLengthVal();
            }
        }
    }
    
    public String getMethodName () { return method_name; }
    public boolean isError () { return (errmsg != null); }
    public String getErrorMessage () { return errmsg; }
    public int getNArguments () { return arguments.size(); }
    public ArgType getArgumentType (int index) { return arguments.get(index).getArgumentType(); }
    public String getArgumentString (int index) throws IllegalArgumentException { return arguments.get(index).getArgumentString(); }
    public String [] getArgumentArrayString (int index) throws IllegalArgumentException { return arguments.get(index).getArgumentArrayString(); }
    public int getArgumentInt (int index) throws IllegalArgumentException { return arguments.get(index).getArgumentInt(); }
    public int [] getArgumentArrayInt (int index) throws IllegalArgumentException { return arguments.get(index).getArgumentArrayInt(); }
    public Date getArgumentDate (int index) throws IllegalArgumentException { return arguments.get(index).getArgumentDate(); }
    public Date [] getArgumentArrayDate (int index) throws IllegalArgumentException { return arguments.get(index).getArgumentArrayDate(); }

    private String extractString (String message, int msg_len, int start_pos, int string_len)
    throws ParseException
    {
        if (start_pos + string_len > msg_len)
            throw new ParseException ("Not enough data in packets", start_pos);
        return message.substring(start_pos, start_pos + string_len);
    }
    
    private LengthString extractLengthString (String message, int start_pos)
    throws ParseException
    {
        String string = findDelimitedString (message, start_pos);
        LengthString length_string = new LengthString(string, start_pos);
        return length_string;
    }
            
    private String findDelimitedString (String string, int start_pos)
    {
        StringBuffer buffer;
        
        buffer = new StringBuffer ();
        for (int count=start_pos; count<string.length(); count++)
        {
            if (string.charAt(count) == ':') break;
            buffer.append (string.charAt(count));
        }
        return buffer.toString();
    }

    
}
