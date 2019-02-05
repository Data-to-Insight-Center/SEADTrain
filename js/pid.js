$(document).ready(function (){
	bool_pagination = true;
	var xhr = new XMLHttpRequest();
	xhr.open("GET", "data/metadata.json", true);
	xhr.onreadystatechange = function() {
	  if (xhr.readyState == 4) {
		// innerText does not let the attacker inject HTML elements.
		myFunction(xhr.responseText);
	  }
	}
xhr.send();
	
});

$("#filter-search-button").click(function () {
	$("#CRUDthisTable").empty();
});

		
function myFunction(response) {
	arr = JSON.parse(response);
	ros = [];
	for(var i = 0; i < arr.length; i++) {
        var title = arr[i].Title;
		var creator = arr[i].Creator;
		var publisher = arr[i].Publisher;
		var publication_date = arr[i]["Publication Date"];
		
		for (key in arr[i]["Registered Clean Data PIDs"]){
			for (clean_key in arr[i]["Registered Clean Data PIDs"][key]){
				var pid = arr[i]["Registered Clean Data PIDs"][key][clean_key];
				var obj_type = clean_key;
				var final_title = title + " as " + obj_type;
				var data_citation = creator + " (2018). " + final_title + ". " + publisher + ". " + pid
				
				ros.push({"CreatorName":creator, "CreationDate":publication_date, "Publisher":publisher, "PublicationDate":publication_date, "Title":final_title, "PersistentIdentifier":pid, "DataCitation":data_citation})
			}
			
		}		
		
		for (raw in arr[i]["Registered Raw Data PIDs"]){
			for (raw_data in arr[i]["Registered Raw Data PIDs"][raw]){
				var pid = arr[i]["Registered Raw Data PIDs"][raw][raw_data];
				var obj_type = raw_data;
				var type = raw;
				
				var final_title = title + " as " + obj_type;
				var data_citation = creator + " (2018). " + final_title + ". " + publisher + ". " + pid
				
				if (obj_type == "December" || obj_type == "November" || obj_type == "October" || obj_type == "September" || obj_type == "August" || obj_type == "July" || obj_type == "June" || obj_type == "May" || obj_type == "January") {
					for (day in pid){
						var new_pid = pid[day];
						var cre_date = day;
						
						var new_type = raw;
						
						var lastIndex = new_type.lastIndexOf(" ");
						//new_type = new_type.substring(0, lastIndex);
						
						var new_final_title = title + " as " + new_type.substring(0, lastIndex) + " Daily Data CSV file";
						var new_data_citation = creator + " (2018). " + new_final_title + ". " + publisher + ". " + new_pid
						
						ros.push({"CreatorName":creator, "CreationDate":cre_date, "Publisher":publisher, "PublicationDate":publication_date, "Title":new_final_title, "PersistentIdentifier":new_pid, "DataCitation":new_data_citation})
					}
				}else{
					ros.push({"CreatorName":creator, "CreationDate":publication_date, "Publisher":publisher, "PublicationDate":publication_date, "Title":final_title, "PersistentIdentifier":pid, "DataCitation":data_citation})
					
				}
			}
		}
		
	}
	
	$("#viewRowTemplate").tmpl(ros).appendTo("#CRUDthisTable");
}
	