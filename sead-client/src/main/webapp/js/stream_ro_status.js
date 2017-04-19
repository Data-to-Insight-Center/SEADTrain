$(document).ready(function (){


    $('.QueryStatus').on('click', function(e, a, b)
    {

        var row = $('#RoStatusForm');

        var ro_id = row.find('#ro-id').val();


        var xmlhttp = new XMLHttpRequest();

        var apiprefix = "./../rest/";
        var url = apiprefix + "streamro/airbox/status";
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
                    //document.getElementById('response_div_code').innerHTML = "\n" + library.json.prettyPrint(JSON.parse(xmlhttp.responseText));
                    var json = JSON.parse(xmlhttp.responseText)
                    var tr;
                            for (var i = 0; i < json.length; i++) {
                                tr = $('<tr/>');
                                tr.append("<td>" + json[i].device_id + "</td>");
                                tr.append("<td>" + json[i].week + "</td>");
                                tr.append("<td>" + json[i].year + "</td>");
                                var innerJson = json[i].status_list;
                                var innerHtml = '<table class="table table-bordered">';
                                for (var j = 0; j < innerJson.length; j++) {
                                    innerHtml += "<tr><td>" + innerJson[j].ro_id + "</td>";
                                    innerHtml += "<td>" + innerJson[j].status + "</td>";
                                    innerHtml += "<td>" + innerJson[j].date + "</td></tr>";
                                }
                                innerHtml += '</table>';
                                tr.append("<td>" + innerHtml + "</td>");
                                $('#stream_ro_response_table').append(tr);
                            }

                    //document.getElementById('response_div').className = "status_successful";
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