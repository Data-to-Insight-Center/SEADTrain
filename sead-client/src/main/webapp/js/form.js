$(document).ready(function (){


    $('.SubmitForm').on('click', function(e, a, b)
    {

        var files = document.getElementById('file-input').files;
        if (!files.length) {
            alert('Please select a file!');
            return;
        }
        var file = files[0];
        //var start = parseInt(opt_startByte) || 0;
        var start = 0;
        //var stop = parseInt(opt_stopByte) || file.size - 1;
        var stop = file.size - 1;

        var reader = new FileReader();

        // If we use onloadend, we need to check the readyState.
        reader.onloadend = function(evt) {
            if (evt.target.readyState == FileReader.DONE) { // DONE == 2
                var data = _.reduce(evt.target.result,
                            function(sum, byte) {
                                return sum + ' 0x' + String(byte).charCodeAt(0).toString(16);
                            }, '');
                //alert(['Read bytes: ', start + 1, ' - ', stop + 1, ' of ', file.size, ' byte file'].join(''));

                var row = $('#RoDetailsForm');

                var metadata = {};
                metadata.TITLE = row.find('#title_input').val();
                metadata.CREATOR = row.find('#creator_input').val();
                metadata.ABSTRACT = row.find('#abstract_input').val();
                metadata.RIGHTS_HOLDER = row.find('#rights_holder_input').val();
                metadata.LICENSE = row.find('#license_input').val();

                metadata.FILE_NAME = row.find('#dataset-name').val();
                metadata.FILE_SIZE = row.find('#dataset-size').val();
                metadata.FILE_TYPE = row.find('#dataset-type').val();
                metadata.LAST_MODIFIED_DATE = row.find('#dataset-modified-date').val();
                metadata.DATA_BYTES = data;

                //alert(JSON.stringify(metadata,null, " "));

                var xmlhttp = new XMLHttpRequest();

                var apiprefix = "./../rest";
                var url = apiprefix + "/rorequest";
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
                       document.getElementById('status_div_code').innerHTML = "\n" + "Loading...";
                       document.getElementById('status_div').className = "status_loading";
                    }
                    else if (xmlhttp.readyState === 4) {                // 4 = Response from server has been completely loaded.
                        if (xmlhttp.status == 200 && xmlhttp.status < 300) { // http status between 200 to 299 are all successful
                            document.getElementById('status_div_code').innerHTML = "\n" + library.json.prettyPrint(JSON.parse(xmlhttp.responseText));
                            document.getElementById('status_div').className = "status_successful";
                        } else {
                            document.getElementById('status_div_code').innerHTML = "\n" + library.json.prettyPrint(JSON.parse(xmlhttp.responseText));
                            document.getElementById('status_div').className = "status_error";
                        }
                    }
                }
                //xmlhttp.setRequestHeader('Content-Type', 'application/json');
                xmlhttp.open("POST", url, true);
                xmlhttp.send(JSON.stringify(metadata));

            }
        };

        var blob;
        if (file.slice) {
            blob = file.slice(start, stop + 1);
        }else if (file.webkitSlice) {
            blob = file.webkitSlice(start, stop + 1);
        } else if (file.mozSlice) {
            blob = file.mozSlice(start, stop + 1);
        }
        console.log('reader: ', reader);
        reader.readAsBinaryString(blob);



    });

    $('.ResetForm').on('click', function(e, a, b) {
        document.getElementById('status_div').className = "status_hidden";
    });
});