function showImage() {
	var book_image_show_flag = $("#book_image_show_flag").val();
	if (book_image_show_flag == "0") {
		$("#book_image_id").show();
		$("#book_image_show_flag").val("1");
	} else {
		$("#book_image_id").hide();
		$("#book_image_show_flag").val("0");
	}
}