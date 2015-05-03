            <span class="bottom"></span>
            </div>
            </div>
      </div>
      <div class="modal"><div class="modal_text"><img src="css/images/ajax-loader.gif"/><div id="wait_text">text...</div></div></div>
      <div id="login_div" class="ontopwindows">
       		<img src="css/images/closex.png" class="closewindowbutton" onclick='closeLoginWindow()'>
       		<div class="ontopwindow_heading">Login</div>
       		<form id="login_form">
       		
       			<input type="hidden" id="my_callback" name="my_callback" class="input_field"/>
       			<input type="text" id="j_ajax_username" name="j_ajax_username" class="input_field"/>
				<input type="password" id="j_ajax_password" name="j_ajax_password" class="input_field"/>
				<div id="ajax_login_error"></div>
       		</form>
			<br/>
			<input type="button" value="Login" onclick="ajaxLogin()">
     	</div>