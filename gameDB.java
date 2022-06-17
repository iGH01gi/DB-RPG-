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
			
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE); //resultset�� Ŀ�� ��ġ�� �յڷ� ����� �����ϼ� �ְ��ϸ� resultset�� �����ϴµ� ��밡���ϰ���
			
			System.out.println("1.USER�� ���� 2.���� �����ڷ� ���� 3.����:");
			
			
			Scanner scanner = new Scanner(System.in);
			int input=scanner.nextInt();
			scanner.nextLine();
			if(input==3)
			{
				System.out.println("���α׷��� �����մϴ�.");
				System.exit(0); //��������
				
			}
			
			else if(input==1) //user
			{
				System.out.println("�α����� ID�� �Է����ּ���:");
				String id=scanner.next();
				System.out.println("�α����� PW�� �Է����ּ���:");
				String pw=scanner.next();
				
				ResultSet rset=stmt.executeQuery("select ID from account_suspended"); //�������� ���̵�� �޼��� ����� ����
				while(rset.next()) //������ ���� ���߳� ����Ȯ��.
				{
					String temp=rset.getString("ID");
					if(id.equals(temp))
					{
						System.out.println("������ �������� �����Դϴ�. ���α׷��� �����մϴ�.");
						System.exit(0); //��������
					}										
				}
				
				rset=stmt.executeQuery("select * from user");
				while(rset.next())
				{
					String tem_id=rset.getString("ID");
					String tem_pw=rset.getString("PW");
					if(id.equals(tem_id)&&pw.equals(tem_pw))//DB�� �ִ� id,����϶��� �������
					{
						System.out.println("���̵�� ��й�ȣ�� ��ġ�մϴ�. \n��������͸� ��õ�ް� ���� ĳ������ �̸��� �Է����ּ���:");
						String nickn=scanner.next();
						PreparedStatement pStmt=conn.prepareStatement("select map_name from map where required_level=(select max(required_level)from map where required_level<all(select level from game_character where nick_name=?)&&? in (select nick_name from game_character))");
						pStmt.setString(1,nickn);
						pStmt.setString(2,nickn);
						ResultSet rs;
						rs=pStmt.executeQuery();
						if(rs.next()==false)
						{
							System.out.println("�Է��� �г����� �������� �ʽ��ϴ�. ���α׷��� �����մϴ�.");
							System.exit(0); //��������
						}
						System.out.println("���� �ش� ĳ���Ϳ��� ���� ������ �����(��)�� : "+rs.getString("map_name"));
						System.out.println("���α׷��� ���������� �������ϴ�. �����մϴ�.");
						rs.close();
						System.exit(0); //��������
					}				
				}
				System.out.println("���̵�� ��й�ȣ�� Ʋ�Ƚ��ϴ�. �����մϴ�.");
				System.exit(0); //��������
				
				
				
			}
			
			else if(input==2) //���� ������
			{
				ResultSet rset=stmt.executeQuery("select ID from account_suspended");
				
				ResultSet check;
				PreparedStatement pStmt=conn.prepareStatement("select ID from user where ? in (select ID from user)");
						
				System.out.println("������ �ְ��� �ϴ� ������ ID:");
				String id=scanner.next();
				pStmt.setString(1, id);
				check=pStmt.executeQuery();
				if(check.next()==false)
				{
					System.out.println("�ش� ID�� ���� user�� �������� �ʽ��ϴ�. ���α׷��� �����մϴ�.");
					System.exit(0); //��������
				}
				
				
				System.out.println("���� ���� ��¥(ex.2021-10-11):");
				String start=scanner.next();
				System.out.println("���� ���� ��¥(ex.2021-10-11):");
				String end=scanner.next();
				System.out.println("���� ����:");
				String reason=scanner.next();
				
				while(rset.next()) //������ ���� ���߳� ����Ȯ��.
				{
					String temp=rset.getString("ID");
					if(id.equals(temp)) //������ �����ϸ� �̹� �������Ѱ���. ���� ���ο� Ʃ���� �߰��ϴ� ���� �ƴ� ���� ����Ⱓ�� ������
					{
						PreparedStatement ps;
						pStmt=conn.prepareStatement("select * from account_suspended where ID=?");
						pStmt.setString(1,id);
						ResultSet rs;
						rs=pStmt.executeQuery();
						rs.next();
						System.out.println("������ �������� �����Դϴ�. ���� ����Ⱓ�� �����մϴ�.\n");
						System.out.println("ID:" + rs.getString("ID")+"    start_time:"+rs.getDate("start_time")+"    end_time:"+rs.getDate("end_time")+"   reason:"+rs.getString("reason"));
						System.out.println("������ �����Ű�ڽ��ϱ�?(�� ���� �Է�):\n");
						int extend=scanner.nextInt();
						ps=conn.prepareStatement("update account_suspended set end_time=DATE_ADD((select end_time from(select end_time from account_suspended where account_suspended.ID=?)T),INTERVAL ? DAY) where ID=?");
						ps.setString(1, id);
						ps.setInt(2, extend);
						ps.setString(3, id);
						ps.executeUpdate();
						rs=pStmt.executeQuery();
						rs.next();
						
						;
						System.out.println("���� �� ���´�->ID:" + rs.getString(1)+"      start_time:"+rs.getDate(2)+"      end_time:"+rs.getDate(3)+"      reason:"+rs.getString(4));
						System.out.println("\n�����մϴ�.");
						
						System.exit(0); //��������
					}										
				}
				
					
					System.exit(0); //��������
				
			}
		
			
			
		}
		catch(SQLException sqle) 
		{
			System.out.println("SQLException : "+sqle);
			System.out.println("�����߰�.");
		}
	}
} 

