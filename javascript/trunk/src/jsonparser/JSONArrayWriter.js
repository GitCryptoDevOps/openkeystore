/*
 *  Copyright 2006-2014 WebPKI.org (http://webpki.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

/*================================================================*/
/*                         JSONArrayWriter                        */
/*================================================================*/

 org.webpki.json.JSONArrayWriter = function (optional_array)
{
   /* Vector<org.webpki.json.JSONValue> */this.array = optional_array === undefined ? [] : optional_array;
};

/* org.webpki.json.JSONArrayWriter */org.webpki.json.JSONArrayWriter.prototype._add = function (/* org.webpki.json.JSONTypes */type, /* Object */value)
{
    this.array[this.array.length] = new org.webpki.json.JSONValue (type, value);
    return this;
};

/* public org.webpki.json.JSONArrayWriter */org.webpki.json.JSONArrayWriter.prototype.setString = function (/* String */value)
{
    return this._add (org.webpki.json.JSONTypes.STRING, value);
};

/* public org.webpki.json.JSONArrayWriter */org.webpki.json.JSONArrayWriter.prototype.setInt = function (/* int */value)
{
    return this._add (org.webpki.json.JSONTypes.INTEGER, value);
};

/*
    public org.webpki.json.JSONArrayWriter setLong (long value) throws IOException
      {
        return add (org.webpki.json.JSONTypes.INTEGER, Long.toString (value));
      }

    public org.webpki.json.JSONArrayWriter setBigDecimal (BigDecimal value) throws IOException
      {
        return add (org.webpki.json.JSONTypes.INTEGER, value.toString ());
      }

    public org.webpki.json.JSONArrayWriter setBigInteger (BigInteger value) throws IOException
      {
        return add (org.webpki.json.JSONTypes.INTEGER, value.toString ());
      }

    public org.webpki.json.JSONArrayWriter setDouble (double value) throws IOException
      {
        return add (org.webpki.json.JSONTypes.DOUBLE, Double.toString (value));
      }

    public org.webpki.json.JSONArrayWriter setBoolean (boolean value) throws IOException
      {
        return add (org.webpki.json.JSONTypes.BOOLEAN, Boolean.toString (value));
      }

    public org.webpki.json.JSONArrayWriter setNULL () throws IOException
      {
        return add (org.webpki.json.JSONTypes.NULL, "null");
      }

    public org.webpki.json.JSONArrayWriter setDateTime (Date date_time) throws IOException
      {
        return setString (ISODateTime.formatDateTime (date_time));
      }

/* public org.webpki.json.JSONArrayWriter */org.webpki.json.JSONArrayWriter.prototype.setArray = function ()
{
    /* Vector<org.webpki.json.JSONValue> */var new_array = [] /* new Vector<org.webpki.json.JSONValue> () */;
    this._add (org.webpki.json.JSONTypes.ARRAY, new_array);
    return new org.webpki.json.JSONArrayWriter (new_array);
};

/* public org.webpki.json.JSONObjectWriter */org.webpki.json.JSONArrayWriter.prototype.setObject = function ()
{
    /* org.webpki.json.JSONObject */var holder = new org.webpki.json.JSONObject ();
    this._add (org.webpki.json.JSONTypes.OBJECT, holder);
    return new org.webpki.json.JSONObjectWriter (holder);
};

/* public String */org.webpki.json.JSONArrayWriter.prototype.serializeJSONArray = function (/* org.webpki.json.JSONOutputFormats */output_format)
{
    /* org.webpki.json.JSONObject */var dummy = new org.webpki.json.JSONObject ();
    dummy._setArray (new org.webpki.json.JSONValue (org.webpki.json.JSONTypes.ARRAY, this.array));
    return new org.webpki.json.JSONObjectWriter (dummy).serializeJSONObject (output_format);
};