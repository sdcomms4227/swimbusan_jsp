package board;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class BoardDAO {

	Connection conn;
	PreparedStatement pstmt;
	ResultSet rs;

	private Connection getConnection() throws Exception {
		Context init = new InitialContext();
		DataSource ds = (DataSource) init.lookup("java:comp/env/jdbc/swimbusan");
		Connection conn = ds.getConnection();
		return conn;
	}

	private void freeResource() {
		try {
			if (conn != null)
				conn.close();
			if (pstmt != null)
				pstmt.close();
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			System.out.println("freeResource()메소드 내부에서 예외발생 : " + e.toString());
		}
	}

	public int insertBoard(BoardBean boardBean, String boardId) {
		String sql = "";
		int num = 0;
				
		if(boardBean.getBoardSubject()==null || boardBean.getBoardSubject().equals("")) {
			return -1;
		}else if(boardBean.getBoardContent()==null || boardBean.getBoardContent().equals("")) {
			return -2;
		}else if(boardBean.getBoardPw()==null || boardBean.getBoardPw().equals("")) {
			return -3;
		}else if(boardId.equals("gallery") && (boardBean.getBoardFile()==null || boardBean.getBoardFile().equals(""))) {
			return -4;
		}else {
			try {
				conn = getConnection();
				sql = "select max(boardNum) from " + boardId;
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
	
				if (rs.next()) {
					num = rs.getInt(1) + 1;
				} else {
					num = 1;
				}
	
				sql = "insert into " + boardId +"(boardNum,userId,userName,boardPw,boardSubject,boardContent,boardFile,boardRe_ref,boardRe_lev,boardRe_seq,boardCount,boardDate,boardIp,boardCategory)"
						+ "values(?,?,?,?,?,?,?,?,?,?,?,now(),?,?)";
	
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, num);
				pstmt.setString(2, boardBean.getUserId());
				pstmt.setString(3, boardBean.getUserName());
				pstmt.setString(4, boardBean.getBoardPw());
				pstmt.setString(5, boardBean.getBoardSubject());
				pstmt.setString(6, boardBean.getBoardContent());
				pstmt.setString(7, boardBean.getBoardFile());
				pstmt.setInt(8, num);
				pstmt.setInt(9, 0);
				pstmt.setInt(10, 0);
				pstmt.setInt(11, 0);
				pstmt.setString(12, boardBean.getBoardIp());
				pstmt.setString(13, boardBean.getBoardCategory());
	
				return pstmt.executeUpdate();
	
			} catch (Exception e) {
				System.out.println("insertBoard()메소드 내부에서 예외발생 : " + e.toString());
			} finally {
				freeResource();
			}
		}
		
		return 0; 
	}// insertBoard

	public int getBoardCount(String search, String category, String boardId) {
		
		String sql = "";
		int count = 0;

		try {

			conn = getConnection();			

			if(category == null || category.equals("")) {	
				sql = "select count(*) from " + boardId + " where boardSubject like ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "%" + search + "%");
			} else {
				sql = "select count(*) from " + boardId + " where boardSubject like ? and boardCategory like ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "%" + search + "%");
				pstmt.setString(2, "%" + category + "%");				
			}
			
			rs = pstmt.executeQuery();

			if (rs.next()) {
				count = rs.getInt(1);
			}

		} catch (Exception e) {
			System.out.println("getBoardCount()메소드 내부에서 예외발생 : " + e.toString());
		} finally {
			freeResource();
		}

		return count;
		
	}// getBoardCount

	public List<BoardBean> getBoardList(String search, String category, int startRow, int pageSize, String boardId) {
		String sql = "";
		List<BoardBean> boardList = new ArrayList<BoardBean>();
		
		try {

			conn = getConnection();
			
			if(category == null || category.equals("")) {
				//댓글수를 제외한 SQL
				//sql = "select * from " + boardId +  " where boardSubject like ? order by boardRe_ref desc, boardRe_seq asc limit ?, ?";
				
				//댓글수를 포함한 SQL
				sql = "select *"
					+" from "+ boardId + " b"
					+" left join ("
						+ " select count(*) replyCount, boardId replyBoardId, boardNum replyBoardNum"
						+ " from reply"
						+ " group by boardId, boardNum"
						+ " having boardId = '" + boardId + "') r"
					+" on b.boardNum = r.replyBoardNum"
					+" having b.boardSubject like ?"
					+" order by boardRe_ref desc, boardRe_seq"
					+" limit ?, ?";
				
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "%" + search + "%");
				pstmt.setInt(2, startRow);
				pstmt.setInt(3, pageSize);
			}else {				
				//댓글수를 제외한 SQL
				//sql = "select * from " + boardId +  " where boardSubject like ? and boardCategory like ? order by boardRe_ref desc, boardRe_seq asc limit ?, ?";

				//댓글수를 포함한 SQL
				sql = "select *"
					+" from "+ boardId + " b"
					+" left join ("
						+ " select count(*) replyCount, boardId replyBoardId, boardNum replyBoardNum"
						+ " from reply"
						+ " group by boardId, boardNum"
						+ " having boardId = '" + boardId + "') r"
					+" on b.boardNum = r.replyBoardNum"
					+" having b.boardSubject like ?"
					+" and b.boardCategory like ?"
					+" order by boardRe_ref desc, boardRe_seq"
					+" limit ?, ?";
				
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "%" + search + "%");
				pstmt.setString(2, "%" + category + "%");
				pstmt.setInt(3, startRow);
				pstmt.setInt(4, pageSize);
			}
						
			rs = pstmt.executeQuery();

			while (rs.next()) {
				BoardBean boardBean = new BoardBean();

				boardBean.setBoardCategory(rs.getString("boardCategory"));
				boardBean.setBoardContent(rs.getString("boardContent"));
				boardBean.setBoardCount(rs.getInt("boardCount"));
				boardBean.setBoardDate(rs.getTimestamp("boardDate"));
				boardBean.setBoardFile(rs.getString("boardFile"));
				boardBean.setBoardIp(rs.getString("boardIp"));
				boardBean.setBoardNum(rs.getInt("boardNum"));
				boardBean.setBoardPw(rs.getString("boardPw"));
				boardBean.setBoardRe_lev(rs.getInt("boardRe_lev"));
				boardBean.setBoardRe_ref(rs.getInt("boardRe_ref"));
				boardBean.setBoardRe_seq(rs.getInt("boardRe_seq"));
				boardBean.setBoardSubject(rs.getString("boardSubject"));
				boardBean.setUserId(rs.getString("userId"));
				boardBean.setUserName(rs.getString("userName"));
				boardBean.setReplyCount(rs.getInt("replyCount"));

				boardList.add(boardBean);
			}

		} catch (Exception e) {
			System.out.println("getBoardList()메소드 내부에서 예외발생 : " + e.toString());
		} finally {
			freeResource();
		}

		return boardList;
	}// getBoardList

	public void updateCount(int boardNum, String boardId) {
		String sql = "";

		try {

			conn = getConnection();
			sql = "update " + boardId + " set boardCount = boardCount + 1 where boardNum = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, boardNum);
			pstmt.executeUpdate();

		} catch (Exception e) {
			System.out.println("updateCount()메소드 내부에서 예외발생 : " + e.toString());
		} finally {
			freeResource();
		}
	}// updateCount

	public BoardBean getBoard(int boardNum, String boardId) {
		String sql = "";

		BoardBean boardBean = new BoardBean();

		try {

			conn = getConnection();
			sql = "select * from " + boardId + " where boardNum = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, boardNum);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				boardBean.setBoardCategory(rs.getString("boardCategory"));
				boardBean.setBoardContent(rs.getString("boardContent"));
				boardBean.setBoardDate(rs.getTimestamp("boardDate"));
				boardBean.setBoardFile(rs.getString("boardFile"));
				boardBean.setBoardIp(rs.getString("boardIp"));
				boardBean.setBoardNum(rs.getInt("boardNum"));
				boardBean.setBoardPw(rs.getString("boardPw"));
				boardBean.setBoardRe_lev(rs.getInt("boardRe_lev"));
				boardBean.setBoardRe_ref(rs.getInt("boardRe_ref"));
				boardBean.setBoardRe_seq(rs.getInt("boardRe_seq"));
				boardBean.setBoardCount(rs.getInt("boardCount"));
				boardBean.setBoardSubject(rs.getString("boardSubject"));
				boardBean.setUserId(rs.getString("userId"));
				boardBean.setUserName(rs.getString("userName"));
			}
		} catch (Exception e) {
			System.out.println("getBoard()메소드 내부에서 예외발생 : " + e.toString());
		} finally {
			freeResource();
		}

		return boardBean;
	}// getBoard

	public int updateBoard(BoardBean boardBean, String boardId) {
		String sql = "select boardPw from " + boardId + " where boardNum=?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, boardBean.getBoardNum());
			rs = pstmt.executeQuery();
			if (rs.next()) {				
				if(rs.getString("boardPw")!=null && !boardBean.getBoardPw().equals(rs.getString("boardPw"))) {
					return -1;
				}
				if(boardBean.getBoardFile()==null) {
					sql = "update " + boardId + " set boardCategory=?, boardSubject=?, boardContent=? where boardNum=?";					
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, boardBean.getBoardCategory());
					pstmt.setString(2, boardBean.getBoardSubject());
					pstmt.setString(3, boardBean.getBoardContent());
					pstmt.setInt(4, boardBean.getBoardNum());					
				}else {
					sql = "update " + boardId + " set boardCategory=?, boardSubject=?, boardContent=?, boardFile=? where boardNum=?";					
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, boardBean.getBoardCategory());
					pstmt.setString(2, boardBean.getBoardSubject());
					pstmt.setString(3, boardBean.getBoardContent());
					pstmt.setString(4, boardBean.getBoardFile());
					pstmt.setInt(5, boardBean.getBoardNum());
				}
				return pstmt.executeUpdate();
			}
		} catch (Exception e) {
			System.out.println("updateBoard()메소드 내부에서 예외발생 : " + e.toString());
		} finally {
			freeResource();
		}

		return 0;
	}// updateBoard
	
	public int deleteBoard(int boardNum, String boardPw, String boardId) {
		String sql = "select boardPw from " + boardId + " where boardNum=?";
		
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, boardNum);
			rs = pstmt.executeQuery();
			if (rs.next()) {				
				if(boardPw.equals(rs.getString("boardPw"))) {
					sql = "delete from " + boardId + " where boardNum=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setInt(1, boardNum);
					return pstmt.executeUpdate();
				}else {
					return -1;
				}
			}
		} catch (Exception e) {
			System.out.println("deleteBoard()메소드 내부에서 예외발생 : " + e.toString());
		} finally {
			freeResource();
		}
		
		return 0;
	}// deleteBoard

	public int reInsertBoard(BoardBean boardBean, String boardId) {
		String sql = "";
		int num = 0;

		if(boardBean.getBoardContent()==null || boardBean.getBoardContent().equals("")) {
			return -2;
		}else if(boardBean.getBoardPw()==null || boardBean.getBoardPw().equals("")) {
			return -3;
		}else {
			try {
	
				conn = getConnection();
				sql = "select max(boardNum) from " + boardId;
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
	
				if (rs.next()) {
					num = rs.getInt(1) + 1;
				} else {
					num = 1;
				}
	
				sql = "update " + boardId + " set boardRe_seq=boardRe_seq+1 where boardRe_ref=? and boardRe_seq>?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, boardBean.getBoardRe_ref());
				pstmt.setInt(2, boardBean.getBoardRe_seq());
				pstmt.executeUpdate();
	

				sql = "insert into " + boardId + "(boardNum,userId,userName,boardPw,boardSubject,boardContent,boardFile,boardRe_ref,boardRe_lev,boardRe_seq,boardCount,boardDate,boardIp,boardCategory)"
						+ "values(?,?,?,?,?,?,?,?,?,?,?,now(),?,?)";
	
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, num);
				pstmt.setString(2, boardBean.getUserId());
				pstmt.setString(3, boardBean.getUserName());
				pstmt.setString(4, boardBean.getBoardPw());
				pstmt.setString(5, boardBean.getBoardSubject());
				pstmt.setString(6, boardBean.getBoardContent());
				pstmt.setString(7, boardBean.getBoardFile());
				pstmt.setInt(8, boardBean.getBoardRe_ref());
				pstmt.setInt(9, boardBean.getBoardRe_lev() + 1);
				pstmt.setInt(10, boardBean.getBoardRe_seq() + 1);
				pstmt.setInt(11, 0);
				pstmt.setString(12, boardBean.getBoardIp());
				pstmt.setString(13, boardBean.getBoardCategory());
				
				return pstmt.executeUpdate();	
			} catch (Exception e) {
				System.out.println("reInsertBoard()메소드 내부에서 예외발생 : " + e.toString());
			} finally {
				freeResource();
			}
		}
		return 0;
	}// reInsertBoard

}// BoardDAO
