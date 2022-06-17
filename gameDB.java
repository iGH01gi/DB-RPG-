package databaseproject;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;


public class gameDB 
{ 
	public static void main(String[] args) throws ClassNotFoundException, SQLException
	{
		try 
		{
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/game?useUnicode=true&useJDBCCompliantTimezoneShift=true&" 
			+ "useLegacyDatetimeCode=false&serverTimezone=UTC", "root", "iGH17172080");
			
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE); //resultset의 커서 위치를 앞뒤로 맘대로 움직일수 있게하며 resultset을 업뎃하는데 사용가능하게함
			
			System.out.println("1.USER로 입장 2.게임 관리자로 입장 3.종료:");
			
			
			Scanner scanner = new Scanner(System.in);
			int input=scanner.nextInt();
			scanner.nextLine();
			if(input==3)
			{
				System.out.println("프로그램을 종료합니다.");
				System.exit(0); //정상종료
				
			}
			
			else if(input==1) //user
			{
				System.out.println("로그인할 ID를 입력해주세요:");
				String id=scanner.next();
				System.out.println("로그인할 PW를 입력해주세요:");
				String pw=scanner.next();
				
				ResultSet rset=stmt.executeQuery("select ID from account_suspended"); //정지당한 아이디면 메세지 출력후 종료
				while(rset.next()) //계정이 정지 당했나 여부확인.
				{
					String temp=rset.getString("ID");
					if(id.equals(temp))
					{
						System.out.println("계정이 정지당한 상태입니다. 프로그램을 종료합니다.");
						System.exit(0); //정상종료
					}										
				}
				
				rset=stmt.executeQuery("select * from user");
				while(rset.next())
				{
					String tem_id=rset.getString("ID");
					String tem_pw=rset.getString("PW");
					if(id.equals(tem_id)&&pw.equals(tem_pw))//DB상에 있는 id,비번일때만 정상실행
					{
						System.out.println("아이디와 비밀번호가 일치합니다. \n적정사냥터를 추천받고 싶은 캐릭터의 이름을 입력해주세요:");
						String nickn=scanner.next();
						PreparedStatement pStmt=conn.prepareStatement("select map_name from map where required_level=(select max(required_level)from map where required_level<all(select level from game_character where nick_name=?)&&? in (select nick_name from game_character))");
						pStmt.setString(1,nickn);
						pStmt.setString(2,nickn);
						ResultSet rs;
						rs=pStmt.executeQuery();
						if(rs.next()==false)
						{
							System.out.println("입력한 닉네임은 존재하지 않습니다. 프로그램을 종료합니다.");
							System.exit(0); //정상종료
						}
						System.out.println("현재 해당 캐릭터에게 가장 적합한 사냥터(맵)은 : "+rs.getString("map_name"));
						System.out.println("프로그램이 정상적으로 끝났습니다. 종료합니다.");
						rs.close();
						System.exit(0); //정상종료
					}				
				}
				System.out.println("아이디와 비밀번호가 틀렸습니다. 종료합니다.");
				System.exit(0); //정상종료
				
				
				
			}
			
			else if(input==2) //게임 관리자
			{
				ResultSet rset=stmt.executeQuery("select ID from account_suspended");
				
				ResultSet check;
				PreparedStatement pStmt=conn.prepareStatement("select ID from user where ? in (select ID from user)");
						
				System.out.println("정지를 주고자 하는 유저의 ID:");
				String id=scanner.next();
				pStmt.setString(1, id);
				check=pStmt.executeQuery();
				if(check.next()==false)
				{
					System.out.println("해당 ID를 가진 user는 존재하지 않습니다. 프로그램을 종료합니다.");
					System.exit(0); //정상종료
				}
				
				
				System.out.println("정지 시작 날짜(ex.2021-10-11):");
				String start=scanner.next();
				System.out.println("정지 만료 날짜(ex.2021-10-11):");
				String end=scanner.next();
				System.out.println("정지 사유:");
				String reason=scanner.next();
				
				while(rset.next()) //계정이 정지 당했나 여부확인.
				{
					String temp=rset.getString("ID");
					if(id.equals(temp)) //조건을 만족하면 이미 정지당한계정. 따라서 새로운 튜플을 추가하는 것이 아닌 정지 만료기간을 연장함
					{
						PreparedStatement ps;
						pStmt=conn.prepareStatement("select * from account_suspended where ID=?");
						pStmt.setString(1,id);
						ResultSet rs;
						rs=pStmt.executeQuery();
						rs.next();
						System.out.println("계정이 정지당한 상태입니다. 정지 만료기간을 연장합니다.\n");
						System.out.println("ID:" + rs.getString("ID")+"    start_time:"+rs.getDate("start_time")+"    end_time:"+rs.getDate("end_time")+"   reason:"+rs.getString("reason"));
						System.out.println("몇일을 연장시키겠습니까?(일 단위 입력):\n");
						int extend=scanner.nextInt();
						ps=conn.prepareStatement("update account_suspended set end_time=DATE_ADD((select end_time from(select end_time from account_suspended where account_suspended.ID=?)T),INTERVAL ? DAY) where ID=?");
						ps.setString(1, id);
						ps.setInt(2, extend);
						ps.setString(3, id);
						ps.executeUpdate();
						rs=pStmt.executeQuery();
						rs.next();
						
						;
						System.out.println("변경 후 상태는->ID:" + rs.getString(1)+"      start_time:"+rs.getDate(2)+"      end_time:"+rs.getDate(3)+"      reason:"+rs.getString(4));
						System.out.println("\n종료합니다.");
						
						System.exit(0); //정상종료
					}										
				}
				
					
					System.exit(0); //정상종료
				
			}
		
			
			
		}
		catch(SQLException sqle) 
		{
			System.out.println("SQLException : "+sqle);
			System.out.println("오류발견.");
		}
	}
} 

