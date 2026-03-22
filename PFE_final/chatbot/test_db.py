import sqlalchemy
from sqlalchemy import create_engine

urls_to_test = [
    "mysql+pymysql://root:@127.0.0.1:3306/safar_morocco",
    "mysql+pymysql://root@127.0.0.1:3306/safar_morocco",
    "mysql+pymysql://root:@localhost:3306/safar_morocco",
    "mysql+pymysql://root@localhost:3306/safar_morocco",
]

for url in urls_to_test:
    try:
        engine = create_engine(url)
        connection = engine.connect()
        print(f"Success with {url}")
        connection.close()
    except Exception as e:
        print(f"Failed with {url}: {e}")
