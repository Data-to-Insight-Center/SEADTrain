$(document).ready(function (){


    $('.QueryStatus').on('click', function(e, a, b)
    {

        var row = $('#RoStatusForm');

        var ro_id = row.find('#ro-id').val();


        var xmlhttp = new XMLHttpRequest();

        var apiprefix = "./../rest/";
        var url = apiprefix + ro_id + "/status";
        xmlhttp.onreadystatechange=function() {

            if (!library)
               var library = {};

            library.json = {
               replacer: function(match, pIndent, pKey, pVal, pEnd) {
                  var key = '<span class=json-key>';
                  var val = '<span class=json-value>';
                  var str = '<span class=json-string>';
                  var r = pIndent || '';
                  if (pKey)
                     r = r + key + pKey.replace(/[": ]/g, '') + '</span>: ';
                  if (pVal)
                     r = r + (pVal[0] == '"' ? str : val) + pVal + '</span>';
                  return r + (pEnd || '');
                  },
               prettyPrint: function(obj) {
                  var jsonLine = /^( *)("[\w]+": )?("[^"]*"|[\w.+-]*)?([,[{])?$/mg;
                  return JSON.stringify(obj, null, 3)
                     .replace(/&/g, '&amp;').replace(/\\"/g, '&quot;')
                     .replace(/</g, '&lt;').replace(/>/g, '&gt;')
                     .replace(jsonLine, library.json.replacer);
                  }
            };

            if (xmlhttp.readyState < 4) {               // while waiting response from server
               document.getElementById('response_div_code').innerHTML = "\n" + "Loading...";
               document.getElementById('response_div').className = "status_loading";
            }
            else if (xmlhttp.readyState === 4) {                // 4 = Response from server has been completely loaded.
                if (xmlhttp.status == 200 && xmlhttp.status < 300) { // http status between 200 to 299 are all successful
                    document.getElementById('response_div_code').innerHTML = "\n" + library.json.prettyPrint(JSON.parse(xmlhttp.responseText));
                    document.getElementById('response_div').className = "status_successful";
                } else {
                    document.getElementById('response_div_code').innerHTML = "\n" + library.json.prettyPrint(JSON.parse(xmlhttp.responseText));
                    document.getElementById('response_div').className = "status_error";
                }
            }
        }
        //xmlhttp.setRequestHeader('Content-Type', 'application/json');
        xmlhttp.open("GET", url, true);
        xmlhttp.send();

    });
});