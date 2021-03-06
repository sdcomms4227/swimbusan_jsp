<%@page import="java.sql.Timestamp"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="board.BoardBean"%>
<%@page import="board.BoardDAO"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String boardId = (String) session.getAttribute("boardId");
	String pageName = (String) session.getAttribute("boardName");
	String contextPath = request.getContextPath();
	request.setCharacterEncoding("UTF-8");
%>
<jsp:include page="/include/head.jsp" />
<%

	int boardNum = Integer.parseInt(request.getParameter("boardNum"));
	String pageNum = request.getParameter("pageNum");

	BoardDAO boardDAO = new BoardDAO();

	boardDAO.updateCount(boardNum, boardId);

	BoardBean boardBean = boardDAO.getBoard(boardNum, boardId);

	int readNum = boardBean.getBoardNum();
	int readCount = boardBean.getBoardCount();
	String readName = boardBean.getUserName();
	Timestamp readDate = boardBean.getBoardDate();
	String readSubject = boardBean.getBoardSubject();
	String readContent = "";
	String readBoardFile = boardBean.getBoardFile();
	String readId = boardBean.getUserId();
	String readCategory = boardBean.getBoardCategory();

	if (boardBean.getBoardContent() != null) {
		readContent = boardBean.getBoardContent().replace("\r\n", "<br>");
	}
	int readRe_ref = boardBean.getBoardRe_ref();
	int readRe_lev = boardBean.getBoardRe_lev();
	int readRe_seq = boardBean.getBoardRe_seq();

	SimpleDateFormat sdfmt = new SimpleDateFormat("yy-MM-dd HH:mm");

	String userId = (String) session.getAttribute("userId");
%>
<body>
	<jsp:include page="/include/header.jsp" />
	<section class="container body-container py-5">
		<div class="row">
			<div class="col-12">
				<h2><%=pageName%></h2>
			</div>
		</div>
		<!-- 게시판 -->
		<article class="mt-3">
			<table class="table read-table border-bottom">
				<colgroup>
					<col style="width: 70px" />
					<col />
					<col style="width: 70px" />
					<col style="width: 210px" />
					<col style="width: 70px" />
					<col style="width: 70px" />
				</colgroup>
				<tr>
					<td colspan="6" class="h4 p-3 readsubject">
						<%=readSubject%>
						<%
							if(readCategory!=null && !readCategory.equals("")){
						%>
							<small class="text-muted">[<%=readCategory%>]</small>
						<%
							}
						%>
						<div class="h6 mt-3 mb-0 d-lg-none text-right">
							<small class="text-muted"><%=readName%> | <%=sdfmt.format(readDate)%> | <%=readCount%></small>
						</div>
					</td>
				</tr>
				<tr class="d-none d-lg-table-row">
					<th class="align-middle">작성자</th>
					<td><%=readName%></td>
					<th class="align-middle">작성일</th>
					<td><%=sdfmt.format(readDate)%></td>
					<th class="align-middle">조회수</th>
					<td><%=readCount%></td>
				</tr>
				<tr>
					<td colspan="6" class="py-5">
						<%
							if(readBoardFile!=null && !readBoardFile.equals("") && boardId.equals("gallery")){
								String[] fileItems = readBoardFile.split(",");
						%>
							<div id="mainCasousel" class="carousel slide mb-5" data-ride="carousel">
								<ol class="carousel-indicators mb-0">
									<%
										for(int i=0;i<fileItems.length;i++){
									%>					
										<li data-target="mainCasousel" data-slide-to="<%=i%>" <%if(i==0) out.print("class='active'");%>></li>
									<%
										}
									%>
								</ol>
								<div class="carousel-inner">
									<%
										for(int i=0;i<fileItems.length;i++){
									%>						
										<div class='carousel-item <%if(i==0) out.print("active");%>'>
											<img src="<%=contextPath%>/file/<%=fileItems[i]%>" class="d-block w-100" />
										</div>		
									<%
										}
									%>
								</div>
								<a class="carousel-control-prev" href="#mainCasousel" role="button" data-slide="prev">
									<span class="carousel-control-prev-icon" aria-hidden="true"></span>
									<span class="sr-only">Previous</span>
								</a>
								<a class="carousel-control-next" href="#mainCasousel" role="button" data-slide="next">
									<span class="carousel-control-next-icon" aria-hidden="true"></span>
									<span class="sr-only">Next</span>
								</a>
							</div>
						<%
							}
						%>
						<%=readContent%>
					</td>
				</tr>
				<%
					if(readBoardFile!=null && !readBoardFile.equals("")){
						if(boardId.equals("gallery")){
							String[] fileItems = readBoardFile.split(",");
							for(int i=0;i<fileItems.length;i++){
				%>
					<tr>
						<th class="align-middle">첨부 이미지<%=i+1%></th>
						<td colspan="5">
							<img src="<%=contextPath%>/file/<%=fileItems[i]%>" class="mr-2" style="width:60px" />
							<span class="my-2 mr-2"><%=fileItems[i]%></span>
							<a href="<%=contextPath%>/download.do?fileName=<%=fileItems[i]%>" class="btn btn-sm btn-info my-1">다운로드</a>
						</td>
					</tr>
				<%
							}
						}else{
				%>
					<tr>
						<th class="align-middle">첨부파일</th>
						<td colspan="5">
							<%
								String[] fileTypes = readBoardFile.split("\\.");
								if(fileTypes[1].equals("jpg") || fileTypes[1].equals("png")){
							%>
								<img src="<%=contextPath%>/file/<%=readBoardFile%>" class="mr-2" style="width:60px" />
							<%
								}
							%>
							<%=readBoardFile%>
							<a href="<%=contextPath%>/download.do?fileName=<%=readBoardFile%>" class="btn btn-sm btn-info ml-2">다운로드</a>
						</td>
					</tr>
				<%
						}
					}
				%>
			</table>
			<!-- 댓글 -->
			<jsp:include page="reply.jsp" />
			<!-- 댓글 -->
			<div class="text-center my-5">
				<button type="button" class="btn btn-secondary" onclick="location.href='<%=boardId%>.jsp?pageNum=<%=pageNum%>'">목록</button>
				<%
					if (userId != null && userId.equals(readId)) {
				%>
				<button type="button" class="btn btn-warning" onclick="location.href='update.jsp?pageNum=<%=pageNum%>&boardNum=<%=readNum%>'">수정</button>
				<button type="button" class="btn btn-danger" onclick="location.href='delete.jsp?pageNum=<%=pageNum%>&boardNum=<%=readNum%>'">삭제</button>
				<%
					}
					if (userId != null && !boardId.equals("gallery") && !boardId.equals("info")) {
				%>
				<button type="button" class="btn btn-primary" onclick="document.reWriteform.submit()">답글쓰기</button>
				<%
					}
				%>
			</div>			
		</article>
		<!-- 게시판 -->
		<form action="reWrite.jsp" method="post" name="reWriteform">
			<input type="hidden" name="boardCategory" value="<%=readCategory%>" />
			<input type="hidden" name="boardSubject" value="<%=readSubject%>" />
			<input type="hidden" name="boardNum" value="<%=readNum%>" />
			<input type="hidden" name="boardRe_ref" value="<%=readRe_ref%>" />
			<input type="hidden" name="boardRe_lev" value="<%=readRe_lev%>" />
			<input type="hidden" name="boardRe_seq" value="<%=readRe_seq%>" />
		</form>
	</section>
	<jsp:include page="/include/footer.jsp" />
</body>
</html>