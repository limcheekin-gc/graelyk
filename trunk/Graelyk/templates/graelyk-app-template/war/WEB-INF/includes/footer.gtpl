<%
	try{unwrapVariables()}catch(Exception e){e.toString()}
	defaultValue("welcomeName", userData?.displayName ?: user.toString())
	/*try{test = processedController}catch(Exception e){this.processController(false, null)}*/
%>
			</div>
		</div>
		
		<div class="rightAuto columnRight">
			<% try{out << rightColumnIncludes}catch(Exception e){} %>
		</div>
</div>

<!-- start footer -->
<div id="footer" class="fixed1200">
<div class="fixed1200 left">
@FooterTop@Copyright &copy; 2010 Your Website. All rights reserved. Design: <a href="http://www.nodethirtythree.com/">NodeThirtyThree Design</a> and <a href="http://www.scaracco.net/en/article/14/microcss-a-minimalist-framework">MicroCSS</a>
</div>

	<div class="left fixed1200 wrap">
				<div class="left50">
					<div class="footerColumnLeft">
						<div class="left50"><div class="footerColumnBox">
							@FooterColumnA@
						</div></div>
						<div class="right50"><div class="footerColumnBox">
							@FooterColumnB@
						</div></div>
					</div>
				</div>
				<div class="right50">
					<div class="footerColumnRight">
						<div class="left50"><div class="footerColumnBox">
							@FooterColumnC@
						</div></div>
						<div class="right50"><div class="footerColumnBox">
							@FooterColumnD@
						</div></div>
					</div>
				</div>
	</div>

<div class="fixed1200 left">
@FooterBottom@Copyright &copy; 2010 Your Website. All rights reserved. Design: <a href="http://www.nodethirtythree.com/">NodeThirtyThree Design</a> and <a href="http://www.scaracco.net/en/article/14/microcss-a-minimalist-framework">MicroCSS</a>
</div>
</div>
<!-- end footer -->

</div>

</body>
</html>